package com.root.utils

import android.os.Environment

/**
 * Created by Hello on 2017/2/22.
 */

object CommonCmds {
    val SDCardDir: String = Environment.getExternalStorageDirectory().absolutePath

    val AbsBackUpDir = "$SDCardDir/backups/apps/"

    const val MountSystemRW =
            "busybox mount -o rw,remount /\n" +
                    "mount -o rw,remount /system\n" +
                    "busybox mount -o rw,remount /\n" +
                    "mount -o rw,remount /system\n" +
                    "busybox mount -o remount,rw /dev/block/bootdevice/by-name/system /system\n" +
                    "mount -o remount,rw /dev/block/bootdevice/by-name/system /system\n" +
                    "busybox mount -o rw,remount /vendor\n" +
                    "mount -o rw,remount /vendor\n"

    const val DisableSELinux = "setenforce 0;\n"
    const val ResumeSELinux = "setenforce 1;\n"
}
