package com.root.common.shell

import android.util.Log
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by Hello on 2018/01/23.
 */
class KeepShell(private var rootMode: Boolean = true) {
    private var currentIsIdle = true // 是否处于闲置状态
    private var num = 0
    val isIdle: Boolean
        get() {
            return currentIsIdle
        }


    fun checkRoot(): Boolean {
       return ShellExecutor.superUserRuntimeAvailable != "sh"
    }


    //执行脚本
    fun doCmdSync(cmd: String, envs: HashMap<String, String>? = null): String {
        while (num >= 10){
            println("W:$cmd:BUSY!!!")
            return "BUSY!!!"
        }
        num += 1
        var exitCode = 1
        val shellOutputCache = StringBuilder()
        shellOutputCache.clear()
        val builder = ProcessBuilder()
        builder.directory(File("/data/user/0/com.root.system/files/usr/kr-script"))
        builder.environment()["PATH"] =  "/sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin:/system_ext/bin"

        envs?.let {
            for ((key, value) in envs){
                builder.environment()[key] = value
            }
        }
        val rootBinary = ShellExecutor.superUserRuntimeAvailable
        var execContent = cmd
        if (cmd.startsWith("/data/data/com.root.system")){
            val fileObj = File(cmd)
            if (fileObj.exists()) {
                execContent = fileObj.readText()
            }
        }

        try {
            if (rootMode && rootBinary != "sh"){
                builder.command(rootBinary, "-c", execContent)
            } else {
                builder.command("sh","-c", execContent)
            }
            currentIsIdle = false
            try{
                val process = builder.start()
                val output = process.inputStream.bufferedReader().readLines()
                val error = process.errorStream.bufferedReader().readLines()
                exitCode = process.waitFor()
                shellOutputCache.append((output + error).joinToString("\n"))}
                catch (ex: IOException){
                    ex.printStackTrace() }
            println("Env:$envs\nExec:$execContent\nR:$shellOutputCache\nExit:$exitCode")
            return shellOutputCache.toString().trim()
        }
        catch (e: Exception) {
            Log.e("KeepShellAsync", "" + e.message)
            return "error"
        } finally {
            currentIsIdle = true
            num-= 1
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
