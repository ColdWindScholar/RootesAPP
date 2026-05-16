package com.root.system.dialogs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.DialogHelper
import com.root.system.R

class DialogCat(var context: Activity) {
    fun showCatMenu() {
        val view = context.layoutInflater.inflate(R.layout.dialog_cat_operation, null)
        val dialog = DialogHelper.customDialog(context, view)

        // Function to execute power operation
        fun executePowerOperation(cmd: String) {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(cmd)
        }
        
view.findViewById<View>(R.id.power_hot_reboot).setOnClickListener {
   web("http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=dYADi5_wGJBGzWFD-tThPBNsJbYXj305&authKey=pOPxmZKDxdqci%2FTirUOjdRB%2FRQLaKvW6WuZpW1AQTpw%2F4bkcBJnk1hXUBJfnpKCX&noverify=0&group_code=481909038\n")
}

        view.findViewById<View>(R.id.power_recovery).setOnClickListener {
        web("https://t.me/ComputerAssistant2")
        }



    }
    fun web(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
