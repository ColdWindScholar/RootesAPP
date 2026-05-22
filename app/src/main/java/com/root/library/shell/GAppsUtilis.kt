package com.root.library.shell

import com.root.common.shell.KeepShell

class GAppsUtilis {
    fun enable(keepShell: KeepShell) {
        keepShell.doCmdSync(
            "pm enable com.google.android.gsf\n" +
                    "pm enable com.google.android.gsf.login\n" +
                    "pm enable com.google.android.gms\n" +
                    "pm enable com.android.vending\n" +
                    "pm enable com.google.android.play.games\n" +
                    "pm enable com.google.android.syncadapters.contacts"
        )
    }

    fun disable(keepShell: KeepShell) {
        keepShell.doCmdSync(
            "pm disable com.google.android.gsf\n" +
                    "pm disable com.google.android.gsf.login\n" +
                    "pm disable com.google.android.gms\n" +
                    "pm disable com.android.vending\n" +
                    "pm disable com.google.android.play.games\n" +
                    "pm disable com.google.android.syncadapters.contacts"
        )
    }
}