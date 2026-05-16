package com.root.system.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.root.common.shared.FileWrite
import com.root.krscript.model.RunnableNode
import com.root.krscript.ui.DialogLogFragment
import com.root.system.R
import com.root.system.databinding.ActivityCustomCommandBinding
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.nio.charset.Charset

//import com.root.krscript.model.RunnableNode

class ActivityCustomCommand : ActivityBase() {
    private lateinit var binding: ActivityCustomCommandBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomCommandBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBackArrow()

        binding.btnRun.setOnClickListener {
            runCommand()
        }

        binding.btnConfirm.setOnClickListener {
            val title = binding.commandTitle.text?.toString()
            val script = binding.commandScript.text?.toString()
            if (title.isNullOrEmpty()) {
                Toast.makeText(this, "先输入一个标题吧！", Toast.LENGTH_SHORT).show()
            } else if (script.isNullOrEmpty()) {
                Toast.makeText(this, "脚本内容不能为空哦！", Toast.LENGTH_SHORT).show()
            } else {
                saveCommand(title, script)
            }
        }
    }
    
private fun runCommand() {
    val script = binding.commandScript.text?.toString()

    if (script.isNullOrEmpty()) {
        return
    }

    val tempFile = File("/data/data/com.root.system/.shell.sh")

    try {
        // 写入脚本到临时文件
        FileOutputStream(tempFile).use { output ->
            output.write(script.toByteArray(Charset.defaultCharset()))
        }

        // 设置文件可执行
        tempFile.setExecutable(true)

        // 创建 RunnableNode 实例
        val nodeInfo = RunnableNode(currentConfigXml = "") // 传递实际的 XML 配置字符串

        // 创建 DialogLogFragment 实例并执行脚本
        val onExit = Runnable {
            // 执行完毕后删除临时文件
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }

        // 创建 DialogLogFragment
        val dialogFragment = DialogLogFragment.create(
            nodeInfo, 
            onExit,
            {},
            script, 
            null
        )
        dialogFragment.show(supportFragmentManager, "DialogLogFragment")

        // 在新线程中执行脚本
        Thread {
            try {
                val command = arrayOf("sh", tempFile.absolutePath)
                val process = Runtime.getRuntime().exec(command)

                // 读取输出流
                val output = process.inputStream.bufferedReader().use { it.readText() }
                val error = process.errorStream.bufferedReader().use { it.readText() }

                // 等待进程完成
                process.waitFor()

                // 无需在UI线程中显示结果，去掉相关Toast

            } catch (e: Exception) {
                // 无需在UI线程中显示错误提示
            }
        }.start()

    } catch (e: Exception) {
        Toast.makeText(this, "保存脚本失败：${e.message}", Toast.LENGTH_SHORT).show()
    }
}





    private fun saveCommand(title: String, script: String, replace: Boolean = false) {
        val fileContent = script.toByteArray(Charsets.UTF_8)
        val fileName = "custom-command/" + URLEncoder.encode(title, "UTF-8") + ".sh"

        if (FileWrite.writePrivateFile(fileContent, fileName, this)) {
            setResult(RESULT_OK, Intent().apply {
                putExtra("path", fileName)
            })
            finish()
            Toast.makeText(this, "添加成功！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "保存失败！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        title = getString(R.string.menu_custom_command)
    }

    override fun onPause() {
        super.onPause()
    }
}