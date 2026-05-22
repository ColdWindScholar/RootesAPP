package com.root.common.shell

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import com.root.common.shell.ShellExecutor
import java.io.IOException

/**
 * Created by Hello on 2018/01/23.
 */
class KeepShell(private var rootMode: Boolean = true) {
    private var currentIsIdle = true // 是否处于闲置状态
    val rootBinaries = listOf("su", "suu", "timesu", "02su", "kp")
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }



    //获取ROOT超时时间
    private val mLock = ReentrantLock()
    private val LOCK_TIMEOUT = 10000L
    private var enterLockTime = 0L

    private var checkRootState =
            // "if [[ \$(id -u 2>&1) == '0' ]] || [[ \$(\$UID) == '0' ]] || [[ \$(whoami 2>&1) == 'root' ]] || [[ \$(\$USER_ID) == '0' ]]; then\n" +
            "if [[ \$(id -u 2>&1) == '0' ]] || [[ \$(\$UID) == '0' ]] || [[ \$(whoami 2>&1) == 'root' ]] || [[ \$(set | grep 'USER_ID=0') == 'USER_ID=0' ]]; then\n" +
                    "  echo 'success'\n" +
                    "else\n" +
                    "if [[ -d /cache ]]; then\n" +
                    "  echo 1 > /cache/vtools_root\n" +
                    "  if [[ -f /cache/vtools_root ]] && [[ \$(cat /cache/vtools_root) == '1' ]]; then\n" +
                    "    echo 'success'\n" +
                    "    rm -rf /cache/vtools_root\n" +
                    "    return\n" +
                    "  fi\n" +
                    "fi\n" +
                    "exit 1\n" +
                    "exit 1\n" +
                    "fi\n"

    fun checkRoot(): Boolean {
        val r = doCmdSync(checkRootState).lowercase(Locale.getDefault())
        return if (r == "error" || r.contains("permission denied") || r.contains("not allowed") || r == "not found") {
            false
        } else if (r.contains("success")) {
            true
        } else {
            false
        }
    }


    private val shellOutputCache = StringBuilder()

    //执行脚本
    fun doCmdSync(cmd: String): String {
        println(cmd)
        if (mLock.isLocked && enterLockTime > 0 && System.currentTimeMillis() - enterLockTime > LOCK_TIMEOUT) {
            Log.e("doCmdSync-Lock", "线程等待超时${System.currentTimeMillis()} - $enterLockTime > $LOCK_TIMEOUT")
        }
        val builder = ProcessBuilder()
        val rootBinary = ShellExecutor.superUserRuntimeAvailable
        try {
            if (rootMode && rootBinary != "sh"){
                builder.command("$rootBinary -c '$cmd'")
            } else {
                builder.command("sh -c '$cmd'")
            }
            mLock.lockInterruptibly()
            currentIsIdle = false
            GlobalScope.launch(Dispatchers.IO){
                try{
                val process = builder.start()
                val output = process.inputStream.bufferedReader().readLines()
                val error = process.errorStream.bufferedReader().readLines()
                val exitCode = process.waitFor()
                shellOutputCache.append(output + error)}
                catch (ex: IOException){
                    ex.printStackTrace()
                }
            }
            return shellOutputCache.toString().trim()
        }
        catch (e: Exception) {
            Log.e("KeepShellAsync", "" + e.message)
            return "error"
        } finally {
            enterLockTime = 0L
            mLock.unlock()
            currentIsIdle = true
        }
    }

    // 执行脚本，并对结果进行ResourceID翻译
    fun doCmdSync(shellCommand: String, shellTranslation: ShellTranslation): String {
        val rows = doCmdSync(shellCommand).split("\n")
        return if (rows.isNotEmpty()) {
            shellTranslation.resolveRows(rows)
        } else {
            ""
        }
    }
}
