package com.root.common.shell

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by Hello on 2018/01/23.
 */
class KeepShell(private var rootMode: Boolean = true) {
    private var currentIsIdle = true // 是否处于闲置状态
    val rootBinaries = listOf("su", "suu", "timesu", "02su", "kp")
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }

    private var checkRootState =
            // "if [[ \$(id -u 2>&1) == '0' ]] || [[ \$(\$UID) == '0' ]] || [[ \$(whoami 2>&1) == 'root' ]] || [[ \$(\$USER_ID) == '0' ]]; then\n" +
            "if [[ \$(id -u 2>&1) == '0' ]] || [[ \$(\$UID) == '0' ]] || [[ \$(whoami 2>&1) == 'root' ]] || [[ \$(set | grep 'USER_ID=0') == 'USER_ID=0' ]]; then\n" +
                    "  echo 'success'\n" +
                    "else\n" +
                    "if [[ -d /cache ]]; then\n" +
                    "  echo 1 > /cache/vtools_root\n" +
                    "  if [[ -f /cache/vtools_root ]] && [[ \$(cat /cache/vtools_root) == '1' ]]; then\n" +
                    "    echo 'success'\n" +
                    "    rm -rf /cache/vtools_root\n" +
                    "    return\n" +
                    "  fi\n" +
                    "fi\n" +
                    "exit 1\n" +
                    "exit 1\n" +
                    "fi\n"

    fun checkRoot(): Boolean {
        val r = doCmdSync(checkRootState).lowercase(Locale.getDefault())
        return if (r == "error" || r.contains("permission denied") || r.contains("not allowed") || r == "not found") {
            false
        } else if (r.contains("success")) {
            true
        } else {
            false
        }
    }


    private val shellOutputCache = StringBuilder()

    //执行脚本
    fun doCmdSync(cmd: String, envs: HashMap<String, String>? = null): String {
        println(cmd)

        val builder = ProcessBuilder()
        builder.directory(File("/data/user/0/com.root.system/files/usr/kr-script"))
        builder.environment()["PATH"] =  "/sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin"

        envs?.let {
            for ((key, value) in envs){
                builder.environment()[key] = value
            }
        }
        val rootBinary = ShellExecutor.superUserRuntimeAvailable
        try {
            if (rootMode && rootBinary != "sh"){
                builder.command(rootBinary, "-c", cmd)
            } else {
                builder.command("sh","-c", cmd)
            }
            currentIsIdle = false
            GlobalScope.launch(Dispatchers.IO){
                try{
                val process = builder.start()
                val output = process.inputStream.bufferedReader().readLines()
                val error = process.errorStream.bufferedReader().readLines()
                val exitCode = process.waitFor()
                shellOutputCache.append(output + error)}
                catch (ex: IOException){
                    ex.printStackTrace()
                }
            }
            return shellOutputCache.toString().trim()
        }
        catch (e: Exception) {
            Log.e("KeepShellAsync", "" + e.message)
            return "error"
        } finally {
            currentIsIdle = true
        }
    }

    // 执行脚本，并对结果进行ResourceID翻译
    fun doCmdSync(shellCommand: String, shellTranslation: ShellTranslation): String {
        val rows = doCmdSync(shellCommand).split("\n")
        return if (rows.isNotEmpty()) {
            shellTranslation.resolveRows(rows)
        } else {
            ""
        }
    }
}
