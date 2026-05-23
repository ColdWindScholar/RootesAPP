package com.root.common.shell

import java.io.IOException

object ShellExecutor {
    private val extraEnvPath: String = ""
    var defaultEnvPath = "" // /sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin


    private val envPath: String?
        get() {
            // FIXME:非root模式下，默认的 TMPDIR=/data/local/tmp 变量可能会导致某些需要写缓存的场景（例如使用source指令）脚本执行失败！
            if (extraEnvPath != null && !extraEnvPath.isEmpty()) {
                if (defaultEnvPath.isEmpty()) {
                    try {
                        val process = Runtime.getRuntime().exec("sh")
                        val outputStream = process.outputStream
                        outputStream.write($$"echo $PATH".toByteArray())
                        outputStream.flush()
                        outputStream.close()

                        val inputStream = process.inputStream
                        val cache = ByteArray(16384)
                        val length = inputStream.read(cache)
                        inputStream.close()
                        process.destroy()

                        val path = String(cache, 0, length).trim { it <= ' ' }
                        if (path.length > 0) {
                            defaultEnvPath = path
                        } else {
                            throw RuntimeException($$"未能获取到$PATH参数")
                        }
                    } catch (ex: Exception) {
                        defaultEnvPath = "/sbin:/system/sbin:/system/bin:/system/xbin:/odm/bin:/vendor/bin:/vendor/xbin"
                    }
                }

                val path = defaultEnvPath

                return ("PATH=$path:$extraEnvPath")
            }

            return null
        }

    @Throws(IOException::class)
    private fun getProcess(run: List<String?>?): ProcessBuilder {
        val env = envPath
        val builder = ProcessBuilder()
        builder.environment()["PATH"] = env
        /*
        // 部分机型会有Aborted错误
        if (env != null) {
            return runtime.exec(run, new String[]{
                env
            });
        }
        */
        if (env != null) {
            builder.environment()["PATH"] = env
        }
        val process = builder.command(run)
        return process
    }

    @JvmStatic
    @get:Throws(IOException::class)
    val superUserRuntime: ProcessBuilder get() { return getProcess(listOf(superUserRuntimeAvailable)) }
    val superUserRuntimeAvailable: String
        get() {
            // 依次尝试执行每个命令

            for (command in arrayOf("su", "suu", "timesu", "02su", "kp")) {
                try {
                    // 尝试执行命令
                    Runtime.getRuntime().exec(command)
                    return command
                } catch (e: IOException) {
                    // 如果执行命令失败，继续尝试下一个命令
                }
            }

            return "sh"
        }


    @get:Throws(IOException::class)
    val runtime: ProcessBuilder?
        get() {
            try {
                return getProcess(listOf("sh"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
}