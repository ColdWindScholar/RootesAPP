package com.root.krscript.executor

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.edit
import com.root.common.shared.FileWrite.getPrivateFileDir
import com.root.common.shared.FileWrite.getPrivateFilePath
import com.root.common.shared.FileWrite.writePrivateShellFile
import com.root.common.shared.MagiskExtend
import com.root.common.shell.KeepShell
import com.root.common.shell.KeepShellPublic.checkRoot
import com.root.common.shell.KeepShellPublic.defaultKeepShell
import com.root.common.shell.ShellExecutor
import com.root.common.shell.ShellTranslation
import com.root.krscript.FileOwner
import com.root.krscript.model.NodeInfoBase
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object ScriptEnvironmen {
    private const val ASSETS_FILE = "file:///android_asset/"
    var isInited: Boolean = false
        private set
    private var environmentHashMap: HashMap<String, String> = HashMap()

    // 此目录将添加到PATH尾部，作为应用程序提供的拓展程序库目录，如有需要则需要在初始化executor.sh之前为该变量赋值
    private var TOOKIT_DIR: String? = ""
    private var rooted = false
    private var privateShell: KeepShell? = null
    private var shellTranslation: ShellTranslation? = null
    init {
        rooted = checkRoot()
        privateShell = if (rooted) defaultKeepShell else KeepShell(rooted)
    }

    private fun init(context: Context): Boolean {
        val configSpf = context.getSharedPreferences("kr-script-config", Context.MODE_PRIVATE)

        return init(
            context,
            configSpf.getString("executor", "kr-script/executor.sh")!!,
            configSpf.getString("toolkitDir", "kr-script/toolkit")
        )
    }

    /**
     * 初始化执行器
     *
     * @param context  Context
     * @param executor 执行器在Assets中的位置
     * @return 是否初始化成功
     */
    @JvmStatic
    fun init(context: Context, executor: String, toolkitDir: String?): Boolean {
        if (isInited) {
            return true
        }

        shellTranslation = ShellTranslation(context.applicationContext)


        try {
            if (!toolkitDir.isNullOrEmpty()) {
                TOOKIT_DIR = ExtractAssets(context).extractResources(toolkitDir)
            }

            var fileName = executor
            if (fileName.startsWith(ASSETS_FILE)) {
                fileName = fileName.substring(ASSETS_FILE.length)
            }

            val inputStream = context.assets.open(fileName)
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes, 0, bytes.size)
            var envShell = String(bytes, Charset.defaultCharset()).replace("\r", "")

            val environment = getEnvironment(context)
            for (key in environment.keys) {
                var value = environment[key]
                if (value == null) {
                    value = ""
                }
                envShell = envShell.replace("$({$key})", value)
            }
            envShell = envShell.replace("$({EXECUTOR_PATH})", "fake_value")


            isInited = true
            if (isInited) {
                for (i in envShell.split("\n")){
                    if (i.trim().isNotEmpty()){
                        val (key, value) = i.trim().split("=")
                        environmentHashMap[key] = value
                    }
                }
            }

            context.getSharedPreferences("kr-script-config", Context.MODE_PRIVATE).edit {
                putString("executor", fileName)
                putString("toolkitDir", toolkitDir)
            }


            return isInited
        } catch (ex: Exception) {
            ex.message?.let { Log.d("shell init",it) }
            return false
        }
    }


    /**
     * 执行脚本
     *
     * @param context
     * @param fileName
     * @return
     */
    private fun extractScript(context: Context, fileName: String): String? {
        var fileName = fileName
        if (fileName.startsWith(ASSETS_FILE)) {
            fileName = fileName.substring(ASSETS_FILE.length)
        }
        return writePrivateShellFile(fileName, fileName, context)
    }

    @JvmStatic
    fun executeResultRoot(context: Context, script: String?, nodeInfoBase: NodeInfoBase?): String {
        if (!isInited) {
            init(context)
        }

        if (script.isNullOrEmpty()) {
            return ""
        }

        val script2 = script.trim { it <= ' ' }


        if (!isInited) {
            init(context)
        }


        if (nodeInfoBase != null && !nodeInfoBase.currentPageConfigPath.isEmpty()) {
            val parentPageConfigDir = nodeInfoBase.pageConfigDir
            val currentPageConfigPath = nodeInfoBase.currentPageConfigPath
            environmentHashMap["PAGE_CONFIG_DIR"] = parentPageConfigDir
            environmentHashMap["PAGE_CONFIG_FILE"] = currentPageConfigPath
            if (currentPageConfigPath.startsWith("file:///android_asset/")) {
                environmentHashMap["PAGE_WORK_DIR"] = ExtractAssets(context).getExtractPath(parentPageConfigDir)
                environmentHashMap["PAGE_WORK_FILE"] = ExtractAssets(context).getExtractPath(currentPageConfigPath)
            } else {
                environmentHashMap["PAGE_WORK_DIR"] = parentPageConfigDir
                environmentHashMap["PAGE_WORK_FILE"] = currentPageConfigPath
            }
        } else {
            for (i in listOf("PAGE_CONFIG_DIR", "PAGE_CONFIG_FILE", "PAGE_WORK_DIR")){
                environmentHashMap[i] = ""
            }
        }

        val scriptContent = if (script2.startsWith(ASSETS_FILE)) {
            extractScript(context, script2)
        } else {
           script
        }
        val retContent = if (shellTranslation != null) {
            shellTranslation!!.resolveRow(
                privateShell!!.doCmdSync(scriptContent!!, envs =  environmentHashMap)
            )
        } else {
            privateShell!!.doCmdSync(scriptContent!!, envs =  environmentHashMap)
        }
        println("RC:$retContent")
        return retContent
    }

    private fun getStartPath(context: Context): String {
        val dir = getPrivateFileDir(context)
        if (dir.endsWith("/")) {
            return dir.substring(0, dir.length - 1)
        }
        return dir
    }

    /*
    public static int getUserId() {
        int value = 0;
        try {
            Class<?> c = Class.forName("android.os.UserHandle");
            Method get = c.getMethod("getUserId", int.class);
            value = (int)(get.invoke(c, android.os.Process.myUid()));
        } catch (Exception ignored) {
        }
        return value;
    }*/
    /**
     * 获取框架的环境变量
     *
     * @param context
     * @return
     */
    private fun getEnvironment(context: Context): HashMap<String?, String?> {
        val params = HashMap<String?, String?>()

        params["TOOLKIT"] = TOOKIT_DIR
        if (MagiskExtend.moduleInstalled()) {
            val magiskPath = if (MagiskExtend.MAGISK_PATH.endsWith("/")) (MagiskExtend.MAGISK_PATH.substring(
                0,
                MagiskExtend.MAGISK_PATH.length - 1
            )) else MagiskExtend.MAGISK_PATH
            params["MAGISK_PATH"] = magiskPath
        } else {
            params["MAGISK_PATH"] = ""
        }
        params["START_DIR"] = getStartPath(context)
        // params.put("EXECUTOR_PATH", environmentPath);
        params["TEMP_DIR"] = context.cacheDir.absolutePath

        val fileOwner = FileOwner(context)
        val androidUid = fileOwner.getUserId()
        params["ANDROID_UID"] = "" + androidUid

        try {
            // @ https://blog.csdn.net/Gaugamela/article/details/78689580
            params["APP_USER_ID"] = fileOwner.getFileOwner()
            // params.put("APP_UID", "" + android.os.Process.myPid());
            // params.put("APP_PID", "" + android.os.Process.myPid());
            // params.put("APP_TID", "" + android.os.Process.myTid());
        } catch (ignored: Exception) {
        }

        params["ANDROID_SDK"] = "" + Build.VERSION.SDK_INT
        // params.put("ROOT_PERMISSION", rooted ? "granted" : "denied");
        params["ROOT_PERMISSION"] = rooted.toString()
        params["SDCARD_PATH"] = Environment.getExternalStorageDirectory().absolutePath
        val busyboxPath = getPrivateFilePath(context, "busybox")
        if (File(getPrivateFilePath(context, "busybox")).exists()) {
            params["BUSYBOX"] = busyboxPath
        } else {
            params["BUSYBOX"] = "toybox"
        }
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            params["PACKAGE_NAME"] = context.packageName
            params["PACKAGE_VERSION_NAME"] = packageInfo.versionName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params["PACKAGE_VERSION_CODE"] = "" + packageInfo.longVersionCode
            } else {
                params["PACKAGE_VERSION_CODE"] = "" + packageInfo.versionCode
            }
        } catch (ex: Exception) {
        }

        return params
    }

    /**
     * @param params
     * @return
     */



    @JvmStatic
    val runtime: Process?
        get() {
            try {
                val builder = ProcessBuilder()
                if (rooted) {
                    val commands = arrayOf<String?>("su", "suu", "kp", "02su", "timesu")

                    // 依次尝试执行每个命令
                    for (command in commands) {
                        try {
                            // 尝试执行命令
                            val process = builder.command(command)

                            // 检查是否获得root权限 (例如通过检查某个标志或命令的输出)
                            // 这里简单地返回了process，你可以在此处进行更复杂的检查
                            return process.start()
                        } catch (e: IOException) {
                            // 如果执行命令失败，继续尝试下一个命令
                            e.printStackTrace()
                        }
                    }

                    // 如果所有命令都失败，抛出异常或返回null
                    throw IOException("Failed to obtain root access using su, suu, or timesu.")
                } else {
                    return builder.command("sh").start()
                }
            } catch (ex: Exception) {
                return null
            }
        }

    /**
     * 使用执行器运行脚本
     *
     * @param context          Context
     * @param cmds             要执行的脚本
     * @param params           参数类别
     */
    @JvmStatic
    fun executeShell(
        context: Context?,
        cmds: String,
        params: HashMap<String?, String?>?,
        nodeInfo: NodeInfoBase?,
        tag: String?,
        rootMode: Boolean = true
    ):Process {
        var params = params
        if (params == null) {
            params = HashMap()
        }

        // 页面配置文件路径
        if (nodeInfo != null) {
            val parentPageConfigDir = nodeInfo.pageConfigDir
            val currentPageConfigPath = nodeInfo.currentPageConfigPath
            params["PAGE_CONFIG_DIR"] = parentPageConfigDir
            params["PAGE_CONFIG_FILE"] = currentPageConfigPath
            if (currentPageConfigPath.startsWith("file:///android_asset/")) {
                params["PAGE_WORK_DIR"] = ExtractAssets(context).getExtractPath(parentPageConfigDir)
                params["PAGE_WORK_FILE"] = ExtractAssets(context).getExtractPath(currentPageConfigPath)
            } else {
                params["PAGE_WORK_DIR"] = parentPageConfigDir
                params["PAGE_WORK_FILE"] = currentPageConfigPath
            }
        } else {
            params["PAGE_CONFIG_DIR"] = ""
            params["PAGE_CONFIG_FILE"] = ""
            params["PAGE_WORK_DIR"] = ""
            params["PAGE_WORK_FILE"] = ""
        }

        val builder = ProcessBuilder()
        if (params.isNotEmpty()) {
            for (param in params.keys) {
                builder.environment()[param] = params[param]
            }
        }
        println(cmds)
        val execBinary =if (rootMode) ShellExecutor.superUserRuntimeAvailable else "sh"
        builder.command(execBinary, "-c", cmds)
        return builder.start()
    }
}