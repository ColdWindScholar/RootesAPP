package com.root.system.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.root.system.R
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Element
import org.w3c.dom.Node

class ActivityAPPID : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appid)

        val container = findViewById<LinearLayout>(R.id.app_info_container)
        if (container == null) {
            Log.e("ActivityAPPID", "容器加载失败，R.id.app_info_container 未找到")
            Toast.makeText(this, "容器加载失败", Toast.LENGTH_SHORT).show()
            return
        } else {
            Log.d("ActivityAPPID", "容器加载成功")
        }

        val userId = getUserId()
        val filePath = "/data/system/users/$userId/settings_ssaid.xml"
        val convertedFilePath = "${filesDir}/settings_ssaid_converted.xml"

        if (convertAbxToXml(filePath, convertedFilePath)) {
            parseXMLAndDisplay(convertedFilePath)
        }
    }

    private fun getUserId(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id -u"))
            val reader = process.inputStream.bufferedReader()
            val userId = reader.readText().trim()
            process.waitFor()
            userId
        } catch (e: Exception) {
            Log.e("ActivityAPPID", "获取用户 ID 失败: ${e.message}")
            "0" // 默认使用用户 0
        }
    }

    private fun convertAbxToXml(inputPath: String, outputPath: String): Boolean {
        return try {
            val command = "abx2xml $inputPath $outputPath"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.waitFor()
            File(outputPath).exists().also { exists ->
                if (!exists) {
                    Log.e("ActivityAPPID", "转换后的文件不存在: $outputPath")
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityAPPID", "转换失败: ${e.message}")
            false
        }
    }

    private fun parseXMLAndDisplay(filePath: String) {
        Log.d("ActivityAPPID", "开始解析 XML 文件")

        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("ActivityAPPID", "文件不存在: $filePath")
                return
            }

            val inputStream = file.inputStream()
            val saxParserFactory = SAXParserFactory.newInstance()
            val saxParser = saxParserFactory.newSAXParser()

            val handler = object : DefaultHandler() {
                private var currentElement = ""
                private var currentPackageName = ""
                private var currentValue = ""

                override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                    currentElement = qName
                    if (qName == "setting") {
                        currentPackageName = attributes.getValue("package")
                        currentValue = attributes.getValue("value")
                    }
                }

                override fun endElement(uri: String, localName: String, qName: String) {
                    if (qName == "setting") {
                        try {
                            val packageInfo = packageManager.getPackageInfo(currentPackageName, 0)
                            val appName = packageInfo.applicationInfo?.loadLabel(packageManager).toString()
                            val appIcon = packageInfo.applicationInfo?.loadIcon(packageManager)

                            val itemView = layoutInflater.inflate(R.layout.app_info_item, findViewById(R.id.app_info_container), false)
                            itemView.findViewById<ImageView>(R.id.app_icon).setImageDrawable(appIcon)
                            itemView.findViewById<TextView>(R.id.app_name).text = appName
                            itemView.findViewById<TextView>(R.id.app_id).text = currentValue

                            itemView.setOnClickListener {
                                showIdDialog(appName, currentValue)
                            }

                            findViewById<LinearLayout>(R.id.app_info_container).addView(itemView)
                        } catch (e: Exception) {
                            Log.e("ActivityAPPID", "获取应用信息失败: ${e.message}")
                        }
                    }
                }
            }

            val inputSource = InputSource(inputStream)
            saxParser.parse(inputSource, handler)
        } catch (e: Exception) {
            Log.e("ActivityAPPID", "XML 解析失败: ${e.message}")
        }
    }

 private fun showIdDialog(appName: String, currentId: String) {
    val options = arrayOf("随机ID", "自定义ID")

    // 使用系统 API 创建对话框
    val builder = AlertDialog.Builder(this)
    builder.setTitle("选择ID类型")

    // 创建对话框
    val dialog = builder.create()

    // 设置单选项
    builder.setSingleChoiceItems(options, -1) { _, which ->
        when (which) {
            0 -> {
                updateXmlWithNewId(appName, generateRandomId())
                dialog.dismiss() // 处理完后关闭对话框
            }
            1 -> {
                showCustomIdInputDialog(appName)
                dialog.dismiss() // 处理完后关闭对话框
            }
        }
    }

    // 设置对话框的取消按钮
    builder.setNegativeButton("取消") { _, _ ->
        dialog.dismiss() // 取消时关闭对话框
    }

    // 显示对话框
    dialog.show()
 }


    private fun showCustomIdInputDialog(appName: String) {
        val input = EditText(this)
        input.hint = "请输入自定义ID"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("输入自定义ID")
        builder.setView(input)
        builder.setPositiveButton("确定") { dialog, _ ->
            val customId = input.text.toString().trim()
            if (customId.isNotBlank()) {
                updateXmlWithNewId(appName, customId)
                Toast.makeText(this, "自定义ID已更新: $customId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "自定义ID不能为空", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()  // 确保对话框在输入后关闭
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()  // 确保在取消时对话框关闭
        }
        builder.setCancelable(true)
        builder.show()
    }

    private fun generateRandomId(): String {
        return (100000..999999).random().toString()
    }

    private fun updateXmlWithNewId(appName: String, newId: String) {
        val xmlFilePath = "/data/system/users/${getUserId()}/settings_ssaid.xml"  // 目标 XML 文件路径
        val tempFilePath = "${filesDir}/settings_ssaid_converted.xml"  // 临时文件路径

        try {
            // 使用 root 权限读取 XML 文件
            val readCommand = "cat $xmlFilePath > $tempFilePath"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", readCommand))
            process.waitFor()

            // 解析 XML 文件
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.parse(File(tempFilePath))

            // 查找并更新对应的 ID 和 defaultValue
            val nodeList = document.getElementsByTagName("setting")
            var found = false
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element
                    val packageName = element.getAttribute("package")
                    if (packageName == appName) {
                        found = true
                        Log.d("ActivityAPPID", "Updating ID for $appName from ${element.getAttribute("value")} to $newId")

                        // 更新 value 和 defaultValue
                        element.setAttribute("value", newId)
                        element.setAttribute("defaultValue", newId)
                        break
                    }
                }
            }

            if (!found) {
                Log.e("ActivityAPPID", "未找到匹配的 package: $appName")
                return
            }

            // 将修改后的文档写入临时文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val domSource = DOMSource(document)
            val streamResult = StreamResult(File(tempFilePath))
            transformer.transform(domSource, streamResult)

            Log.d("ActivityAPPID", "XML 写入成功")

            // 使用 root 权限将临时文件移到目标路径并覆盖原文件
            val moveCommand = "mv $tempFilePath $xmlFilePath"
            val chmodCommand = "chmod 644 $xmlFilePath"
            Runtime.getRuntime().exec(arrayOf("su", "-c", moveCommand)).waitFor()
            Runtime.getRuntime().exec(arrayOf("su", "-c", chmodCommand)).waitFor()

            Log.d("ActivityAPPID", "XML 更新成功: $xmlFilePath")

        } catch (e: Exception) {
            Log.e("ActivityAPPID", "更新 XML 失败: ${e.message}")
        }
    }
}
