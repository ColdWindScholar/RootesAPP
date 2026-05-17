package com.root

import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.root.common.shared.FileWrite
import com.root.common.shell.ShellExecutor
import com.root.data.EventBus
import com.root.data.customer.ChargeCurve
import com.root.data.customer.PowerUtilizationCurve
import com.root.data.customer.ScreenOffCleanup
import com.root.data.publisher.BatteryState
import com.root.data.publisher.ScreenState
import com.root.permissions.CheckRootStatus
import com.root.scene_mode.TimingTaskManager
import com.root.scene_mode.TriggerIEventMonitor
import com.root.store.SpfConfig
import com.root.utils.CrashHandler
import com.root.system.R
import androidx.core.content.edit

class Scene : Application() {
    companion object {
        private val handler = Handler(Looper.getMainLooper())
        lateinit var context: Application
        lateinit var thisPackageName: String
        private var nightMode = false
        private var config: SharedPreferences? = null
        val globalConfig: SharedPreferences
            get() {
                if (config == null) {
                    config = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, MODE_PRIVATE)
                }
                return config!!
            }

        val isNightMode: Boolean
            get() {
                return nightMode
            }

        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return globalConfig.getBoolean(key, defaultValue)
        }

        fun setBoolean(key: String, value: Boolean) {
            globalConfig.edit { putBoolean(key, value) }
        }

        fun getString(key: String, defaultValue: String): String? {
            return globalConfig.getString(key, defaultValue)
        }

        fun toast(message: String, time: Int) {
            handler.post {
                Toast.makeText(context, message, time).show()
            }
        }

        fun toast(message: String) {
            handler.post {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun toast(message: Int, time: Int) {
            handler.post {
                Toast.makeText(context, message, time).show()
            }
        }

        fun post(runnable: Runnable) {
            handler.post(runnable)
        }

        fun postDelayed(runnable: Runnable, delayMillis: Long) {
            handler.postDelayed(runnable, delayMillis)
        }
    }

    // 锁屏状态监听
    private lateinit var screenState: ScreenState

    private var lastThemeId = R.style.AppTheme
    private fun setAppTheme(theme: Int) {
        if (lastThemeId != theme) {
            setTheme(theme)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        nightMode = ((newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0)
    }

    override fun onCreate() {
        super.onCreate()

        // 设置主题
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES) {
            setAppTheme(R.style.AppThemeNight)
        } else {
            setAppTheme(R.style.AppTheme)
        }

        context = this
        thisPackageName = this.packageName
        CrashHandler().init(this)



        // 锁屏状态检测
        screenState = ScreenState(this)
        screenState.autoRegister()

        // 电池状态检测
        BatteryState(context).registerReceiver()

        // 定时任务
        TimingTaskManager(this).updateAlarmManager()

        // 事件任务
        EventBus.subscribe(TriggerIEventMonitor(this))

        // 充电曲线
        EventBus.subscribe(ChargeCurve(this))
        // 耗电曲线
        EventBus.subscribe(PowerUtilizationCurve(this))

        // 息屏自动关闭悬浮窗
        EventBus.subscribe(ScreenOffCleanup(context))

        // 如果上次打开应用成功获得root，触发一下root权限申请
        if (getBoolean("root", false)) {
            CheckRootStatus.checkRootAsync()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}