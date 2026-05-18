package com.root.api

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class ADBManager(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    companion object {
        private const val TAG = "ADBManager"
        private const val ACTION_USB_PERMISSION = "com.root.system.USB_PERMISSION"
    }

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted && device != null) {
                    Log.d(TAG, "Permission granted for device: ${device.deviceName}")
                    callback?.invoke(device, true)
                } else {
                    Log.e(TAG, "Permission denied for device")
                    callback?.invoke(device, false)
                }
                context?.unregisterReceiver(this)
            }
        }
    }

    private var callback: ((UsbDevice?, Boolean) -> Unit)? = null

    /**
     * 自动检测所有已连接的设备
     */
    private fun getConnectedDevices(): List<String> {
        val devices = mutableListOf<String>()
        try {
            // 执行 adb devices 命令
            val process = Runtime.getRuntime().exec("adb devices")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            // 跳过 adb 版本信息行
            reader.readLine()

            // 读取设备列表
            while (reader.readLine().also { line = it } != null) {
                if (line?.contains("\tdevice") == true) {
                    val deviceId = line.split("\t")[0]
                    deviceId.let { devices.add(it) }
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching connected devices: ${e.message}")
        }
        return devices
    }

    /**
     * 请求 USB 设备权限
     */
    fun requestPermission(device: UsbDevice, callback: (UsbDevice?, Boolean) -> Unit) {
        this.callback = callback
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(permissionReceiver, filter)
        usbManager.requestPermission(device, permissionIntent)
    }

    /**
     * 执行 adb shell 命令
     */
    private fun executeShellCommand(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing shell command: ${e.message}")
            null
        }
    }

    /**
     * 自动执行 ADB 命令（无需 Fastboot）
     */
    fun autoExecuteADBCommand(command: String, callback: (String?) -> Unit) {
        val devices = getConnectedDevices()

        if (devices.isNotEmpty()) {
            val deviceId = devices[0] // 使用第一个设备

            try {
                // 切换设备进入 ADB 模式（如果设备在 Fastboot 模式，则通过 ADB 进入）
                val adbCommand = "adb -s $deviceId devices" // 确保设备在线
                executeShellCommand(adbCommand)

                // 执行指定的 ADB 命令
                val adbResponse = executeShellCommand("adb -s $deviceId shell $command")
                callback(adbResponse)

            } catch (e: Exception) {
                Log.e(TAG, "Error during ADB command execution: ${e.message}")
                callback(null)
            }
        } else {
            Log.e(TAG, "No devices found")
            callback(null)
        }
    }

    /**
     * 自动执行 ADB 进入 bootloader 模式
     */
    fun rebootToBootloader(callback: (String?) -> Unit) {
        val devices = getConnectedDevices()

        if (devices.isNotEmpty()) {
            val deviceId = devices[0] // 使用第一个设备

            try {
                // 重启设备进入 Bootloader 模式（相当于 Fastboot 模式）
                val rebootCommand = "adb -s $deviceId reboot bootloader"
                executeShellCommand(rebootCommand)

                // 等待设备进入 bootloader 模式，执行相关命令
                val bootloaderResponse = executeShellCommand("adb -s $deviceId shell fastboot devices")
                callback(bootloaderResponse)

            } catch (e: Exception) {
                Log.e(TAG, "Error rebooting to bootloader: ${e.message}")
                callback(null)
            }
        } else {
            Log.e(TAG, "No devices found")
            callback(null)
        }
    }
}



//使用


//val adbManager = ADBManager(context)

// 执行任意 ADB 命令
//adbManager.autoExecuteADBCommand("getprop ro.build.version.release") { response ->
  //  if (response != null) {
    //    Log.d("ADB", "Device response: $response")
    //} else {
      //  Log.e("ADB", "Failed to execute command")
   // }
//}

//dependencies {
  //  implementation 'com.github.anaconda:android-adb:1.0.0'
//}
