package com.root.library.shell

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import com.root.common.shell.KeepShell
import com.root.common.shell.KeepShellPublic
import com.root.common.shell.RootFile.fileExists

/**
 * 帧率检测
 */
class FpsUtils(private val keepShell: KeepShell = KeepShellPublic.secondaryKeepShell) {
    private var fpsFilePath: String? = null
    private var subStrCommand = "| awk '{print \$2}'"

    private var fpsCommand2 = "service call SurfaceFlinger 1013"
    private var lastTime = -1L
    private var lastFrames = -1

    private var frameCount = 0
    private var lastFrameTime = 0L
    private var choreographer: Choreographer? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        startFrameCallback()
    }

    val currentFps: String?
        get() {
            // 优先使用GPU的内核级帧数数据
            if (!fpsFilePath.isNullOrEmpty()) {
                return keepShell.doCmdSync("cat $fpsFilePath $subStrCommand")
            }
            // 如果系统帧率不可用使用GPU的内核级帧数数据
            else if (fpsFilePath == null) {
                when {
                    fileExists("/sys/class/drm/sde-crtc-0/measured_fps") -> {
                        fpsFilePath = "/sys/class/drm/sde-crtc-0/measured_fps"
                    }
                    fileExists("/sys/class/graphics/fb0/measured_fps") -> {
                        fpsFilePath = "/sys/class/graphics/fb0/measured_fps"
                        subStrCommand = ""
                    }
                    else -> {
                        fpsFilePath = ""
                        val keepShell = KeepShell()
                        Thread {
                            try {
                                keepShell.doCmdSync("find /sys -name measured_fps 2>/dev/null")
                                    .trim { it <= ' ' }.split("\n").filter { it.contains("crtc") }.min()?.run {
                                        fpsFilePath = this
                                    }

                                if (fpsFilePath == null || fpsFilePath == "") {
                                    keepShell.doCmdSync("find /sys -name fps 2>/dev/null")
                                        .trim { it <= ' ' }.split("\n").filter { it.contains("crtc") }.min()?.run {
                                            fpsFilePath = this
                                        }
                                }
                                if (fpsFilePath == null) {
                                    fpsFilePath = ""
                                }
                                keepShell.tryExit()
                            } catch (ex: Exception) {
                                fpsFilePath = ""
                            }
                        }.start()
                    }
                }
            }
            // 使用系统帧率
            else if (fpsCommand2.isNotEmpty()) {
                val result = keepShell.doCmdSync(fpsCommand2).trim()
                if (result != "error" && !result.contains("Parcel")) {
                    fpsCommand2 = ""
                } else {
                    try {
                        val index = result.indexOf("(") + 1
                        val frames = Integer.parseInt(result.substring(index, index + 8), 16)
                        val time = System.currentTimeMillis()
                        var fps = 0F
                        if (lastTime > 0 && lastFrames > 0) {
                            fps = (frames - lastFrames) * 1000.0f / (time - lastTime)
                        }
                        lastFrames = frames
                        lastTime = time
                        return String.format("%.1f", fps)
                    } catch (ex: Exception) {
                        if (!(lastTime > 0 && lastFrames > 0)) {
                            fpsCommand2 = ""
                        }
                    }
                }
            }
            // 如果shell命令也无法获取FPS，则使用Choreographer API
            return if (fpsCommand2.isEmpty()) String.format("%.1f", calculateFps()) else null
        }

    val fps: Float
        get() {
            val fpsStr = currentFps
            if (fpsStr != null) {
                try {
                    return fpsStr.toFloat()
                } catch (ex: Exception) {
                }
            }
            return -0f
        }

    private fun startFrameCallback() {
        choreographer = Choreographer.getInstance()
        choreographer?.postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val currentTime = System.currentTimeMillis()
                if (lastFrameTime != 0L) {
                    val timeDiff = currentTime - lastFrameTime
                    if (timeDiff >= 1000) {
                        lastFrameTime = currentTime
                        frameCount = 0
                    }
                } else {
                    lastFrameTime = currentTime
                }
                choreographer?.postFrameCallback(this)
            }
        })
    }

    private fun calculateFps(): Float {
        return if (lastFrameTime != 0L) frameCount * 1000f / (System.currentTimeMillis() - lastFrameTime) else 0f
    }

    fun stopFrameCallback() {
        choreographer?.removeFrameCallback { }
    }
}