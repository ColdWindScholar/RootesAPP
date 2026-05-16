package com.root.system.dialogs

import android.app.Activity
import android.view.View
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.DialogHelper
import com.root.system.R
import com.root.utils.AlipayDonate

class DialogWX(var context: Activity) {
    fun showWXMenu() {
        val view = context.layoutInflater.inflate(R.layout.dialog_wx_operation, null)
        val dialog = DialogHelper.customDialog(context, view)

        // Function to execute power operation
        fun executePowerOperation(cmd: String) {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(cmd)
        }
        
view.findViewById<View>(R.id.power_hot_reboot).setOnClickListener {
     val dialogWXPNG = DialogWXPNG(context)
    dialogWXPNG.showWXPNGMenu()
}

        view.findViewById<View>(R.id.power_recovery).setOnClickListener {
        AlipayDonate(context).jumpAlipay()
        }

    }

}
