package com.root.system.activities

//import com.root.system.R
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Switch
import androidx.core.content.PermissionChecker
import com.projectkr.shell.OpenPageHelper
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.DialogHelper
import com.root.common.ui.ProgressBarDialog
import com.root.data.EventBus
import com.root.data.EventType
import com.root.krscript.model.PageNode
import com.root.store.SpfConfig
import com.root.system.R
import com.root.system.databinding.ActivityOtherSettingsBinding
import com.root.system.dialogs.DialogCat
import com.root.system.dialogs.DialogWX
import com.root.utils.CommonCmds
import com.root.utils.UpdateBeta

import java.io.File

class ActivityOtherSettings : ActivityBase() {
    private lateinit var binding: ActivityOtherSettingsBinding
    private lateinit var spf: SharedPreferences
    private var myHandler = Handler(Looper.getMainLooper())
private val startFilePath = "/data/data/com.root.system"

    override fun onPostResume() {
        super.onPostResume()
        delegate.onPostResume()

        binding.settingsDisableSelinux.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        spf = getSharedPreferences(SpfConfig.GLOBAL_SPF, MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        binding = ActivityOtherSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
val progressBarDialog = ProgressBarDialog(this)
        setBackArrow()


    val switch: Switch = findViewById(R.id.settings_log)
val filePath = "/data/data/com.root.system/.updeta"
        val file = File(filePath)


        val switch2: Switch = findViewById(R.id.settings_up)
        val filePath2 = "/data/data/com.root.system/.up"
        val file2 = File(filePath2)
        // 初始化 Switch 状态
        switch.isChecked = file.exists()

        // 设置 Switch 监听器
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 用户打开 Switch，创建文件
                createStartFile()
            } else {
                // 用户关闭 Switch，删除文件
                deleteStartFile()
            }
        }

        switch2.isChecked = file2.exists()

        // 设置 Switch 监听器
        switch2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 用户打开 Switch，创建文件
                createStartFile1()
            } else {
                // 用户关闭 Switch，删除文件
                deleteStartFile1()
            }
        }
        
        
        binding.navMagisk.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "KrScript脚本设置"
                pageConfigPath = "/data/data/com.root.system/files/usr/pages/GJZS.xml"
            }
            OpenPageHelper(this).openPage(pageNode)
        }

        binding.navWx.setOnClickListener {
            val dialogWXPNG = DialogWX(this)
            dialogWXPNG.showWXMenu()
        }

        binding.navCat.setOnClickListener {
            val dialogCat = DialogCat(this)
            dialogCat.showCatMenu()
        }

        binding.navAbout.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "作者女装大佬"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/aboutshell.sh"
            }
            OpenPageHelper(this).openPage(pageNode)
        }

        binding.navBug.setOnClickListener {
            val intent = Intent(this, ActionPageOnline::class.java) 
            intent.putExtra("url", "http://www.rootes.top/bug/bug.html") 
            startActivity(intent)
        }

        binding.navNull.setOnClickListener {
            val intent = Intent(this, ActivityAbout::class.java)
            startActivity(intent)
        }

        binding.settingsDisableSelinux.setOnClickListener {
            if (binding.settingsDisableSelinux.isChecked) {
                KeepShellPublic.doCmdSync(CommonCmds.DisableSELinux)
                myHandler.postDelayed({
                    spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, binding.settingsDisableSelinux.isChecked).apply()
                }, 10000)
            } else {
                KeepShellPublic.doCmdSync(CommonCmds.ResumeSELinux)
                spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, binding.settingsDisableSelinux.isChecked).apply()
            }
        }
        binding.settingsLogcat.setOnClickListener {
       //     progressBarDialog.showDialog("正在检查测试版")
            UpdateBeta().checkUpdate(this)
        }



        binding.settingsDebugLayer.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_SCENE_LOG, false)
        binding.settingsDebugLayer.setOnClickListener {
            spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_SCENE_LOG, (it as Switch).isChecked).apply()

            EventBus.publish(EventType.SERVICE_DEBUG)
        }

        binding.settingsHelpIcon.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_HELP_ICON, true)
        binding.settingsHelpIcon.setOnClickListener {
            spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_HELP_ICON, (it as Switch).isChecked).apply()
        }

        binding.settingsAutoExit.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_AUTO_EXIT, true)
        binding.settingsAutoExit.setOnClickListener {
            spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_AUTO_EXIT, (it as Switch).isChecked).apply()
        }

        binding.settingsBlackNotification.isChecked = spf.getBoolean(SpfConfig.GLOBAL_NIGHT_BLACK_NOTIFICATION, false)
        binding.settingsBlackNotification.setOnClickListener {
            spf.edit().putBoolean(SpfConfig.GLOBAL_NIGHT_BLACK_NOTIFICATION, (it as Switch).isChecked).apply()
        }
    }

    private fun checkPermission(context: Context, permission: String): Boolean = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED

    private fun hasRWPermission(): Boolean {
        return checkPermission(this.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                &&
                checkPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun onThemeClick(view: View) {
        val tag = view.tag.toString().toInt()
        if (tag == 10 && spf.getInt(SpfConfig.GLOBAL_SPF_THEME, 1) == 10) {
            spf.edit().remove(SpfConfig.GLOBAL_SPF_THEME).apply()
            this.recreate()
        } else {
            if (tag == 10 && !hasRWPermission()) {
                DialogHelper.helpInfo(view.context, "", getString(R.string.wallpaper_rw_permission))
                (view as Switch).isChecked = false
            } else {
                spf.edit().putInt(SpfConfig.GLOBAL_SPF_THEME, tag).apply()
                this.recreate()
            }
        }

    }
    
     fun onClick(v: View?) {
            // Handle generic click events here if needed
        }

    override fun onDestroy() {
        super.onDestroy()

        spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, binding.settingsDisableSelinux.isChecked).apply()
    }

    public override fun onPause() {
        super.onPause()
    }
    
   private fun start(filePath: String): Boolean {
       val file = File(filePath)
       return if (file.exists()) {
           true
   } else {
       false
       }
   }

    // 检查 /sdcard/rootes/.start 文件是否存在
    private fun isStartFileExists(): Boolean {
        val startFile = File(startFilePath)
        return startFile.exists()
    }

    // 创建 /sdcard/rootes/.start 文件
    private fun createStartFile() {
      KeepShellPublic.doCmdSync("echo yes >/data/data/com.root.system/.updeta")
    }

    // 删除 /sdcard/rootes/.start 文件
    private fun deleteStartFile() {
        KeepShellPublic.doCmdSync("rm -rf /data/data/com.root.system/.updeta")
    }


    // 检查 /sdcard/rootes/.start 文件是否存在
    private fun isStartFileExists1(): Boolean {
        val startFile = File(startFilePath)
        return startFile.exists()
    }

    // 创建 /sdcard/rootes/.start 文件
    private fun createStartFile1() {
        KeepShellPublic.doCmdSync("echo yes >/data/data/com.root.system/.up")
    }

    // 删除 /sdcard/rootes/.start 文件
    private fun deleteStartFile1() {
        KeepShellPublic.doCmdSync("rm -rf /data/data/com.root.system/.up")
    }
}
