package com.root.shell_utils;

import android.os.Environment;

import com.root.common.shell.KeepShellPublic;
import com.root.common.shell.RootFile;

public class AppErrorLogcatUtils {
    private String logPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/玩机百宝箱BUG请发给作者.log";

    public String catLogInfo() {
        if (!RootFile.INSTANCE.fileExists(logPath)) {
            return KeepShellPublic.INSTANCE.doCmdSync("logcat -d *:E > \"" + logPath + "\"");
        }
        return KeepShellPublic.INSTANCE.doCmdSync("cat \"" + logPath + "\"");
    }

    public void catLogInfo2File(int pid) {
        KeepShellPublic.INSTANCE.doCmdSync("logcat -d *:E --pid " + pid + " > \"" + logPath + "\"");
    }
}
