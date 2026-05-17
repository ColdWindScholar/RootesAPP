package com.root.system.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.root.Scene
import com.root.common.shared.MagiskExtend
import com.root.common.shell.KeepShellPublic
import com.root.common.shell.KernelProrp
import com.root.common.shell.RootFile
import com.root.common.ui.DialogHelper
import com.root.permissions.CheckRootStatus
import com.root.store.SpfConfig
import com.root.system.ModuleInstaller
import com.root.ui.TabIconHelper2
import com.root.utils.ElectricityUnit
import com.root.utils.Update
import com.root.system.R
import com.root.system.databinding.ActivityMainBinding
import com.root.system.dialogs.DialogMonitor
import com.root.system.dialogs.DialogPower
import com.root.system.fragments.*
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import com.root.system.dialogs.DiagLogin
import androidx.core.content.edit

class ActivityMain : ActivityBase() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var globalSPF: SharedPreferences
    private val client = OkHttpClient()
    private lateinit var tabIconHelper2: TabIconHelper2

    private class ThermalCheckThread(private var context: Activity) : Thread() {
        private fun deleteThermalCopyWarn(onYes: Runnable) {
            Scene.post {
                if (!context.isFinishing) {
                    val view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_thermal, null)
                    val dialog = DialogHelper.customDialog(context, view)
                    view.findViewById<View>(R.id.btn_no).setOnClickListener {
                        dialog.dismiss()
                    }
                    view.findViewById<View>(R.id.btn_yes).setOnClickListener {
                        dialog.dismiss()
                        onYes.run()
                    }
                    dialog.setCancelable(false)
                }
            }
        }

        val zipFilePath = "/data/data/com.root.system/files/startboot.zip"
        override fun run() {
            ModuleInstaller.installModule(context, zipFilePath)
            sleep(500)
            
            if (
                MagiskExtend.magiskSupported() &&
            KernelProrp.getProp("${MagiskExtend.MAGISK_PATH}system/vendor/etc/thermal.current.ini") != ""
            ) {
                when {
                    RootFile.list("/data/thermal/config").size > 0 -> {
                        deleteThermalCopyWarn {
                            KeepShellPublic.doCmdSync(
                                    "chattr -R -i /data/thermal 2> /dev/null\n" +
                                            "rm -rf /data/thermal 2> /dev/null\n" +
                                            "sync;svc power reboot || reboot;"
                            )
                        }
                    }
                    RootFile.list("/data/vendor/thermal/config").size > 0 -> {
                        if (
                                RootFile.fileEquals(
                                        "/data/vendor/thermal/config/thermal-normal.conf",
                                        MagiskExtend.getMagiskReplaceFilePath("/system/vendor/etc/thermal-normal.conf")
                                )
                        ) {
                            return
                        } else {
                            deleteThermalCopyWarn {
                                KeepShellPublic.doCmdSync(
                                        "chattr -R -i /data/vendor/thermal 2> /dev/null\n" +
                                                "rm -rf /data/vendor/thermal 2> /dev/null\n" +
                                                "sync;svc power reboot || reboot;"
                                )
                            }
                        }
                    }
                    else -> return
                }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context

        if (!ActivityStartSplash.finished) {
            val intent = Intent(this.applicationContext, ActivityStartSplash::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
            return
        }

        globalSPF = getSharedPreferences(SpfConfig.GLOBAL_SPF, MODE_PRIVATE)
        if (!globalSPF.contains(SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT)) {
            globalSPF.edit {
                putInt(
                    SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT,
                    ElectricityUnit().getDefaultElectricityUnit(context)
                )
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tabIconHelper2 = TabIconHelper2(binding.tabList, binding.tabContent, this, supportFragmentManager, R.layout.list_item_tab2)

        // 初始化其他标签
        initializeTabs()

        // 检查root访问权限并处理捐赠标签
        checkRootAccess()

        binding.tabContent.adapter = tabIconHelper2.adapter
        binding.tabList.getTabAt(0)?.select() // 默认选中第一个标签

        // 检查Magisk支持和模块
        checkMagiskSupport()

     val diagLogin = DiagLogin(this)
        diagLogin.checkAutoLogin()

        // 设置按钮事件
        setupButtons()
    }


    // 初始化标签
    private fun initializeTabs() {
        tabIconHelper2.newTabSpec(getString(R.string.app_home), getDrawable(R.drawable.app_home)!!, if (CheckRootStatus.lastCheckResult) FragmentHome() else FragmentNotRootHome())
        tabIconHelper2.newTabSpec(getString(R.string.app_nav), getDrawable(R.drawable.app_menu)!!, if (CheckRootStatus.lastCheckResult) FragmentNav() else FragmentNotRootNav())
        tabIconHelper2.newTabSpec(getString(R.string.app_tuner), getDrawable(R.drawable.app_settings)!!, if (CheckRootStatus.lastCheckResult) FragmentCpuModes() else FragmentNotRoot())

        tabIconHelper2.newTabSpec(getString(R.string.app_user), getDrawable(R.drawable.app_like)!!, if (CheckRootStatus.lastCheckResult) FragmentDonate() else FragmentDonate())
    }

    // 使用root权限获取设备序列号
    private fun getDeviceSerialNumberWithRoot() {
        KeepShellPublic.doCmdSync("getprop ro.serialno").trim()

    }

    // 检查root访问权限并更新UI
    private fun checkRootAccess() {
        val serialNumber = getDeviceSerialNumberWithRoot()

        val url = "https://rootes.top/rootes/admin.php"
        val requestBody = FormBody.Builder()
            .add("serial", serialNumber.toString())
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.string().let { body ->
                    val jsonResponse = JSONObject(body)
                    val result = jsonResponse.optString("root", "")

                    // 根据返回结果更新UI
                    runOnUiThread {
                        if (result == "success") {
                            // 添加捐赠标签
                            tabIconHelper2.newTabSpec(getString(R.string.app_donate), getDrawable(R.drawable.app_like)!!, FragmentDonate())
                        } else if (result == "refusal") {
                            // 移除捐赠标签或将其设为不可见
                            removeDonateTab()
                        }
                    }
                }
            }
        })
    }

    // 移除捐赠标签
    private fun removeDonateTab() {
        val donateTabIndex = tabIconHelper2.indexOfTab(getString(R.string.app_donate))
        if (donateTabIndex != -1) {
            tabIconHelper2.removeTabAt(donateTabIndex)
        }
    }

    // 检查Magisk支持和模块
    private fun checkMagiskSupport() {
        if (CheckRootStatus.lastCheckResult) {
            try {
                if (MagiskExtend.magiskSupported() &&
                    !(MagiskExtend.moduleInstalled() || globalSPF.getBoolean("magisk_dot_show", false))
                ) {
                    DialogHelper.confirm(this,
                        getString(R.string.magisk_install_title),
                        getString(R.string.magisk_install_desc),
                        {
                            MagiskExtend.magiskModuleInstall(this)
                        })
                    globalSPF.edit { putBoolean("magisk_dot_show", true) }
                }
            } catch (ex: Exception) {
                DialogHelper.alert(this, getString(R.string.sorry), "启动应用失败\n${ex.message}") {
                    recreate()
                }
            }
            ThermalCheckThread(this).start()
        }
    }

    // 设置按钮事件
    private fun setupButtons() {
        binding.actionGraph.setOnClickListener {
            actionGraph()
        }
        binding.actionPower.setOnClickListener {
            DialogPower(this).showPowerMenu()
        }
        binding.actionSettings.setOnClickListener {
            startActivity(Intent(this, ActivityOtherSettings::class.java))
        }
    }

    private fun actionGraph() {
        if (!CheckRootStatus.lastCheckResult) {
            Toast.makeText(this, getString(R.string.not_root_disabled), Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                DialogMonitor(this).show()
            } else {
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", this.packageName, null)
                Toast.makeText(applicationContext, getString(R.string.permission_float), Toast.LENGTH_LONG).show()
            }
        } else {
            DialogMonitor(this).show()
        }
    }

    override fun onResume() {
        super.onResume()

        val file = File("/data/data/com.root.system/.updeta")
        if (file.isFile()) {
             Update().checkUpdate(this)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {}

    override fun onBackPressed() {
        try {
            when {
                supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
                else -> {
                    excludeFromRecent()
                    super.onBackPressed()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!CheckRootStatus.lastCheckResult) {
            finish()
        }
    }

    override fun onDestroy() {
        supportFragmentManager.fragments.clear()
        super.onDestroy()
    }
}
