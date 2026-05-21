package com.root.shell_utils

import android.os.Environment

import com.root.common.shell.KeepShellPublic

class AppErrorLogcatUtils {
    private val logPath = Environment.getExternalStorageDirectory().absolutePath + "/玩机百宝箱BUG请发给作者.log"

    fun catLogInfo2File(pid: Int) {
        KeepShellPublic.doCmdSync("logcat -d *:E --pid $pid > \"$logPath\"")
    }
}
