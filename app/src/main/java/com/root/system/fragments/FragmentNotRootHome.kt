package com.root.system.fragments

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLES10
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.root.system.R
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

class FragmentNotRootHome : Fragment() {

    private lateinit var textView: TextView
    private val updateInterval: Long = 3000 // 3 秒刷新一次
    private var updateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_not_root_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.tv_info)

        // 开启定时刷新任务
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                updateDeviceInfo()
                delay(updateInterval.milliseconds)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel() // 停止刷新任务
    }

    private fun updateDeviceInfo() {
        val deviceInfo = getDeviceInfo()
        val cpuInfo = getCpuInfo()
        val gpuInfo = getGpuInfo()
        val batteryInfo = getBatteryInfo()
        textView.text = "$deviceInfo\n$cpuInfo\n$gpuInfo\n$batteryInfo"
    }

    private fun getDeviceInfo(): String {
        val sb = StringBuilder()

        // Android版本信息
        sb.append("Android 版本: ${Build.VERSION.RELEASE}\n")
        sb.append("SDK 版本: ${Build.VERSION.SDK_INT}\n")

        // 设备信息
        sb.append("制造商: ${Build.MANUFACTURER}\n")
        sb.append("型号: ${Build.MODEL}\n")
        sb.append("品牌: ${Build.BRAND}\n")
        sb.append("主板: ${Build.BOARD}\n")
        sb.append("硬件: ${Build.HARDWARE}\n")
        sb.append("产品: ${Build.PRODUCT}\n")
        sb.append("设备: ${Build.DEVICE}\n")
        sb.append("显示编号: ${Build.DISPLAY}\n")

        // 内存信息
        val memoryInfo = getMemoryInfo()
        sb.append("总内存: ${memoryInfo.totalMem / (1024 * 1024)} MB\n")
        sb.append("可用内存: ${memoryInfo.availMem / (1024 * 1024)} MB\n")
        sb.append("是否低内存: ${if (memoryInfo.lowMemory) "是" else "否"}\n")

        // 存储信息
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        sb.append("总存储: ${(totalBlocks * blockSize) / (1024 * 1024)} MB\n")
        sb.append("可用存储: ${(availableBlocks * blockSize) / (1024 * 1024)} MB\n")

        // 获取设备开机时间
        val uptimeMillis = SystemClock.uptimeMillis()
        val uptime = formatTime(uptimeMillis)
        sb.append("开机时间: $uptime\n")

        return sb.toString()
    }

    private fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    private fun getCpuInfo(): String {
        val sb = StringBuilder()

        // 支持的处理器架构
        sb.append("支持的处理器架构: ${Build.SUPPORTED_ABIS.joinToString(", ")}\n")

        // Android 不提供直接获取 CPU 频率的 API
        sb.append("CPU 核心数: ${Runtime.getRuntime().availableProcessors()}\n")

        return sb.toString()
    }

    private fun getGpuInfo(): String {
        val sb = StringBuilder()

        // 使用 OpenGL 获取 GPU 信息
        val renderer = GLES10.glGetString(GLES10.GL_RENDERER)
        val vendor = GLES10.glGetString(GLES10.GL_VENDOR)
        val version = GLES10.glGetString(GLES10.GL_VERSION)

        sb.append("GPU 渲染器: $renderer\n")
        sb.append("GPU 供应商: $vendor\n")
        sb.append("OpenGL 版本: $version\n")

        return sb.toString()
    }

    private fun getBatteryInfo(): String {
        val sb = StringBuilder()
        val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        sb.append("\n电池信息:\n")

        // 获取电池容量
        val batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        sb.append("电池容量: $batteryCapacity%\n")

        // 获取电池电量 (总电量 mAh)
        val batteryChargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        if (batteryChargeCounter != -1) {
            sb.append("电池电量: $batteryChargeCounter mAh\n")
        } else {
            sb.append("电池电量: 不可用\n")
        }

        // 获取当前电流 (mA)
        val batteryCurrentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        if (batteryCurrentNow != -1) {
            sb.append("当前电流: ${batteryCurrentNow / 1000.0} mA\n")
        } else {
            sb.append("当前电流: 不可用\n")
        }

        // 电压通过 BroadcastReceiver 获取
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context?.registerReceiver(null, intentFilter)
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        sb.append("电池电压: ${voltage?.div(1000.0) ?: "不可用"} V\n")

        // 温度
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        sb.append("电池温度: ${temperature?.div(10.0) ?: "不可用"} °C\n")

        // 充电状态
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        sb.append("充电状态: ${if (isCharging) "充电中" else "未充电"}\n")

        // 电源连接类型
        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargeType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB 充电"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC 充电"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
            else -> "未知"
        }
        sb.append("电源连接类型: $chargeType\n")

        return sb.toString()
    }

    // 格式化开机时间
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val remainingHours = hours % 24
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60

        return String.format("%d天 %02d:%02d:%02d", days, remainingHours, remainingMinutes, remainingSeconds)
    }

    companion object {
        fun createPage(): Fragment {
            return FragmentNotRootHome()
        }
    }
}
