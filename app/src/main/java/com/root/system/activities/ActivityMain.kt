package com.root.system.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import com.root.permissions.CheckRootStatus
import com.root.store.SpfConfig
import com.root.system.R
import com.root.system.databinding.ActivityMainBinding
import com.root.system.dialogs.DialogMonitor
import com.root.system.dialogs.DialogPower
import com.root.system.fragments.FragmentCpuModes
import com.root.system.fragments.FragmentDonate
import com.root.system.fragments.FragmentHome
import com.root.system.fragments.FragmentNav
import com.root.ui.TabIconHelper2
import com.root.utils.ElectricityUnit
import com.root.utils.Update
import java.io.File

class ActivityMain : ActivityBase() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var globalSPF: SharedPreferences
    private lateinit var tabIconHelper2: TabIconHelper2

    private class ThermalCheckThread : Thread()

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!ActivityStartSplash.finished) {
            val intent = Intent(this.applicationContext, ActivityStartSplash::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
            finish()
            return
        }
        val c = this
        globalSPF = getSharedPreferences(SpfConfig.GLOBAL_SPF, MODE_PRIVATE)
        if (!globalSPF.contains(SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT)) {
            globalSPF.edit {
                putInt(
                    SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT,
                    ElectricityUnit().getDefaultElectricityUnit(c)
                )
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tabIconHelper2 = TabIconHelper2(binding.tabList, binding.tabContent, this, supportFragmentManager, R.layout.list_item_tab2)

        // 初始化其他标签
        tabIconHelper2.newTabSpec(getString(R.string.app_home), AppCompatResources.getDrawable(this, R.drawable.app_home)!!,  FragmentHome())
        tabIconHelper2.newTabSpec(getString(R.string.app_nav), AppCompatResources.getDrawable( this,R.drawable.app_menu)!!, FragmentNav())
        tabIconHelper2.newTabSpec(getString(R.string.app_tuner), AppCompatResources.getDrawable( this,R.drawable.app_settings)!!,  FragmentCpuModes())
        tabIconHelper2.newTabSpec(getString(R.string.app_user), AppCompatResources.getDrawable(this,R.drawable.app_like)!!,  FragmentDonate())
        // 检查root访问权限并处理捐赠标签
        binding.tabContent.adapter = tabIconHelper2.adapter
        binding.tabList.getTabAt(0)?.select() // 默认选中第一个标签

        // 检查Magisk支持和模块
        checkMagiskSupport()


        // 设置按钮事件
        setupButtons()
    }


    // 检查Magisk支持和模块
    private fun checkMagiskSupport() {
        if (CheckRootStatus.lastCheckResult) {
            ThermalCheckThread().start()
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
