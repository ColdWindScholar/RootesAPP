package com.root.system
import com.root.common.ui.DialogHelper
import android.content.Context
import com.root.common.shell.KeepShellPublic
import java.io.File

object ModuleInstaller {

    // 检查并安装模块
    fun installModule(context: Context, zipFilePath: String) {
        val unzipPath = "/data/adb/modules/startboot"
        val unzipCommand = "/system/bin/unzip"
        val magiskCommand = "magisk"
        
        // 检查是否存在 Magisk 命令
        if (File("/sbin/$magiskCommand").exists()) {
            // 如果 Magisk 命令存在，直接返回，不进行任何操作
            return
        }
        
        // 检查 unzip 命令是否存在
        if (!File(unzipCommand).exists()) {
            DialogHelper.alert(
                context = context,
                title = "错误",
                message = "未找到 unzip 命令，无法继续操作"
            )
            return
        }
        
        // 创建目录并解压
        KeepShellPublic.doCmdSync("mkdir -p $unzipPath")
        val result = executeUnzip(zipFilePath, unzipPath)
        
        // 显示解压结果提示
        if (result) {
            DialogHelper.alert(
                context = context,
                title = "安装完成",
                message = "模块已成功解压到 $unzipPath"
            )
        } else {
            DialogHelper.alert(
                context = context,
                title = "错误",
                message = "解压失败，请检查文件和路径"
            )
        }
    }

    // 解压文件的具体逻辑
    private fun executeUnzip(zipFilePath: String, unzipPath: String): Boolean {
        val unzipCommand = "/system/bin/unzip"
        val result = KeepShellPublic.doCmdSync("$unzipCommand $zipFilePath -d $unzipPath")
        return result.isNotEmpty()
    }

    // 使用 Shell 判断文件是否存在

}
