package com.root.system.dialogs

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.root.common.ui.DialogHelper
import com.root.system.R
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class DiagRec(private val context: Context) {
    private lateinit var dialog: DialogHelper.DialogWrap
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDownloadUrl: EditText

    fun showUploadDialog() {
        val view = (context as Activity).layoutInflater.inflate(R.layout.dialog_upload, null)
        dialog = DialogHelper.customDialog(context, view)
            .setCancelable(false)

        etTitle = view.findViewById(R.id.et_title)
        etDescription = view.findViewById(R.id.et_description)
        etDownloadUrl = view.findViewById(R.id.et_download_url)

        setupButtons(view)
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btn_upload).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val downloadUrl = etDownloadUrl.text.toString().trim()

            if (title.isEmpty() || description.isEmpty() || downloadUrl.isEmpty()) {
                Toast.makeText(context, "请输入所有信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            executeUpload(title, description, downloadUrl)
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun executeUpload(title: String, description: String, downloadUrl: String) {
        val url = "http://www.rootes.top/rootes/upload.php" // 替换成你的服务器地址
        val client = OkHttpClient()

        // 构建请求参数
        val postData = "title=$title&description=$description&download_url=$downloadUrl"
        val body = RequestBody.create("application/x-www-form-urlencoded".toMediaType(), postData)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        // 执行请求
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    if (::dialog.isInitialized) {
                        dialog.dismiss()
                    }
                    showErrorToast("无法连接至服务器")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                (context as Activity).runOnUiThread {
                    if (::dialog.isInitialized) {
                        dialog.dismiss()
                    }
                    handleUploadResponse(responseData)
                }
            }
        })
    }

    private fun handleUploadResponse(responseData: String) {
        val jsonResponse = JSONObject(responseData)
        val status = jsonResponse.getString("status")
        val message = jsonResponse.getString("message")

        if (status == "success") {
            Toast.makeText(context, "资源上传成功(审核完毕以后发布在仓库里面)", Toast.LENGTH_SHORT).show()
        } else {
            showErrorToast(message)
            showUploadDialog()
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
