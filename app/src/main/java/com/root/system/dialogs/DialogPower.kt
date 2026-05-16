package com.root.system.dialogs

import android.app.Activity
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.DialogHelper
import com.root.system.R
import android.view.View

class DialogPower(var context: Activity) {
    fun showPowerMenu() {
        val view = context.layoutInflater.inflate(R.layout.dialog_power_operation, null)
        val dialog = DialogHelper.customDialog(context, view)

        // Function to execute power operation
        fun executePowerOperation(cmd: String) {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(cmd)
        }

        // Set up click listeners with confirmation dialog
        view.findViewById<View>(R.id.power_shutdown).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_shutdown_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }

        view.findViewById<View>(R.id.power_reboot).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_reboot_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }

        view.findViewById<View>(R.id.power_hot_reboot).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_hot_reboot_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }

        view.findViewById<View>(R.id.power_recovery).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_recovery_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }

        view.findViewById<View>(R.id.power_fastboot).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_fastboot_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }

        view.findViewById<View>(R.id.power_emergency).setOnClickListener {
            DialogHelper.confirm(
                context,
                "是否确定选择操作？\n请保存好数据文件！",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    executePowerOperation(context.getString(R.string.power_emergency_cmd))
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
        }
    }
}
