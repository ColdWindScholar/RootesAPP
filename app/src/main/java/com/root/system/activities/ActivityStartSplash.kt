package com.root.system.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.edit
import com.root.Scene
import com.root.common.ui.DialogHelper
import com.root.common.ui.ThemeMode
import com.root.kr.KrScriptConfig
import com.root.library.permissions.GeneralPermissions
import com.root.permissions.CheckRootStatus
import com.root.permissions.WriteSettings
import com.root.store.SpfConfig
import com.root.system.R
import com.root.system.SignCheck
import com.root.system.databinding.ActivityStartSplashBinding
import com.root.utils.BatteryWidgetService
import com.root.utils.InfoWidgetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

class ActivityStartSplash : Activity() {
    companion object {
        var finished = false
    }

    private lateinit var globalSPF: SharedPreferences
    private lateinit var signCode: String
    private lateinit var signCheck: SignCheck
    private lateinit var binding: ActivityStartSplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        globalSPF = getSharedPreferences(SpfConfig.GLOBAL_SPF, MODE_PRIVATE)

        val themeMode = ThemeSwitch.switchTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityStartSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateThemeStyle(themeMode)
        //解码 Base64 字符串
        try {
            signCode = "3F:5C:FB:CA:7E:6E:9F:CA:31:66:62:5B:27:3A:03:8B:F3:9F:4E:85"
        } catch (e: IllegalArgumentException) {
            Log.e("ActivityStartSplash", "Base64 decode error", e)
            Toast.makeText(this, "非官方软件，请到官方网站下载！", Toast.LENGTH_SHORT).show()
            exitProcess(0)
        }

        // 初始化 SignCheck
        signCheck = SignCheck(this, signCode)


        // 检查权限
       checkPermissions()

        val serviceIntent = Intent(this, BatteryWidgetService::class.java)
        startService(serviceIntent)
        val serviceIntent1 = Intent(this, InfoWidgetService::class.java)
        startService(serviceIntent1)
        // 验证签名
        if (!signCheck.check()) {
            Log.e("ActivityStartSplash", "SignCheck failed")
            Toast.makeText(this, "非官方玩机百宝箱，请到官方网站下载！rootes.top", Toast.LENGTH_LONG).show()
        }


    }
    /**
     * 协议 同意与否
     */
 private fun initContractAction() {
    val view = layoutInflater.inflate(R.layout.dialog_danger_agreement, null)
    val dialog = DialogHelper.customDialog(this, view, false)

    val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
    val agreement = view.findViewById<CompoundButton>(R.id.agreement)
    val webView = view.findViewById<WebView>(R.id.web_view)

    // 加载 HTML 内容
    webView.loadUrl("https://coldwindscholar.github.io/RootEs.html")

    val timer = Timer()
    var timeout = 5
    var clickItems = 0

    timer.schedule(object : TimerTask() {
        override fun run() {
            Scene.post {
                if (timeout > 0) {
                    timeout --
                    btnConfirm.text = "$timeout s"
                } else {
                    timer.cancel()
                    btnConfirm.text = "同意继续"
                }
            }
        }
    }, 0, 1000)

    view.findViewById<View>(R.id.btn_cancel).setOnClickListener {
        timer.cancel()
        dialog.dismiss()
        finish()
    }

    btnConfirm.setOnClickListener {
        if (!agreement.isChecked) {
            return@setOnClickListener
        }
        if (timeout > 0 && clickItems < 10000) { // 连点10次允许跳过倒计时
            clickItems++
            return@setOnClickListener
        }

        timer.cancel()
        dialog.dismiss()
        globalSPF.edit { putBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, true) }
        checkPermissions()
        val serviceIntent = Intent(this, BatteryWidgetService::class.java)
        startService(serviceIntent)

        val serviceIntent1 = Intent(this, InfoWidgetService::class.java)
        startService(serviceIntent1)
    }
}

    /**
     * 界面主题样式调整
     */
    private fun updateThemeStyle(themeMode: ThemeMode) {
        window.navigationBarColor = getColorAccent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = getColor(R.color.splash_bg_color)
        } else {
            window.navigationBarColor = resources.getColor(R.color.splash_bg_color)
        }
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            //让应用主题内容占用系统状态栏的空间,注意:下面两个参数必须一起使用 stable 牢固的
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            //设置状态栏颜色为透明
            window.statusBarColor = Color.TRANSPARENT
        }
        //  得到当前界面的装饰视图
        val decorView = window.decorView
        //让应用主题内容占用系统状态栏的空间,注意:下面两个参数必须一起使用 stable 牢固的
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        //设置状态栏颜色为透明
        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= 21) {
            val decorView = window.decorView
            // 设置系统UI标志以支持透明导航栏和状态栏
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            decorView.systemUiVisibility = option

            // 设置状态栏和导航栏颜色为透明
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }

    }

    private fun getColorAccent(): Int {
        val typedValue = TypedValue()
        //this.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
        return typedValue.data
    }

    /**
     * 开始检查必需权限
     */
   private fun checkPermissions() { checkRoot() }

    private class CheckFileWrite(private val context: ActivityStartSplash) : Runnable {
        override fun run() {
            context.binding.startStateText.text = "检查并获取必需权限……"
            context.hasRoot = true

            context.checkFileWrite {
                context.startToFinish()
            }
        }
    }

    private fun checkPermission(permission: String): Boolean = PermissionChecker.checkSelfPermission(this.applicationContext, permission) == PermissionChecker.PERMISSION_GRANTED

    /**
     * 检查权限 主要是文件读写权限
     */
    private fun checkFileWrite(next: Runnable) {
        val activity = this
        GlobalScope.launch(Dispatchers.Main) {
            if (hasRoot) {
                GeneralPermissions(activity).grantPermissions()
            }

            if (!(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                                    Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Manifest.permission.WAKE_LOCK
                            ),
                            0x11
                    )
                } else {
                    ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                                    Manifest.permission.WAKE_LOCK
                            ),
                            0x11
                    )
                }
            }

            // 请求写入设置权限
            val writeSettings = WriteSettings()
            if (!writeSettings.checkPermission(applicationContext)) {
                if (hasRoot) {
                    writeSettings.setPermissionByRoot(applicationContext)
                } else {
                    writeSettings.requestPermission(applicationContext)
                }
            }
            next.run()
        }
    }

    private var hasRoot = false

    private fun checkRoot() {
        val disableSeLinux = globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, false)
        CheckRootStatus(this, {
            if (globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, false)) {
                CheckFileWrite(this).run()
            } else {
                initContractAction()
            }
        }, disableSeLinux, { this.startToFinish() }).forceGetRoot()
    }

    /**
     * 启动完成
     */

    private fun startToFinish() {
        copyAssetsToFiles()
        KrScriptConfig().init(this)
        gotoHome()
    }

    private fun gotoHome() {
        // 使用 Activity context 并用 CLEAR_TOP|SINGLE_TOP 将已有的 ActivityMain 带到前台，保证它位于返回栈中
        val intent = Intent(this, ActivityMain::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finished = true
        finish()
    }

    private fun copyAssetsToFiles() {
    for (filename in assets.list("")!!) {
        binding.startStateText.text = "Copying $filename..."
        // 排除executor.sh文件
        if (filename == "executor.sh") {
            continue
        }
        try {
            copyFile(assets.open(filename), FileOutputStream(File(filesDir, filename)))
        } catch (e: IOException) {
            binding.startStateText.text = e.message
        } finally {
        }
    }
}

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(10000)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        inputStream.close()
        outputStream.close()
    }
    
}
