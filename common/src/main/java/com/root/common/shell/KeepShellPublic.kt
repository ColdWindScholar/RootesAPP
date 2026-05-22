package com.root.common.shell

/**
 * Created by Hello on 2018/01/23.
 */
object KeepShellPublic {
    private val keepShells = HashMap<String, KeepShell>()

    fun getInstance(key: String, rootMode: Boolean): KeepShell {
        synchronized(keepShells) {
            if (!keepShells.containsKey(key)) {
                keepShells[key] = KeepShell(rootMode)
            }
            return keepShells[key]!!
        }
    }



    val defaultKeepShell = KeepShell()



    fun doCmdSync(commands: List<String>): Boolean {
        val stringBuilder = StringBuilder()

        for (cmd in commands) {
            stringBuilder.append(cmd)
            stringBuilder.append("\n\n")
        }

        return doCmdSync(stringBuilder.toString()) != "error"
    }

    //执行脚本
    fun doCmdSync(cmd: String): String {
        return defaultKeepShell.doCmdSync(cmd)
    }

    //执行脚本
    fun checkRoot(): Boolean {
        return defaultKeepShell.checkRoot()
    }


}
