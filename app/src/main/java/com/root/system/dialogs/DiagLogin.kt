package com.root.system.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.root.common.ui.DialogHelper
import com.root.system.R
import com.root.system.SharedPreferencesHelper
import com.root.system.activities.ActionPageOnline
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DiagLogin(private val context: Context) {
    private lateinit var dialog: DialogHelper.DialogWrap
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
   // diagLogin: DiagLogin, java: Class<ActionPageOnline>): Intent?
    // 检查是否已经登录
    fun checkAutoLogin() {
        val email = SharedPreferencesHelper.getUserEmail(context) // 从SharedPreferences读取已保存的邮箱
        val password = SharedPreferencesHelper.getUserPassword(context) // 从SharedPreferences读取已保存的密码

        if (email.isNotEmpty() && password.isNotEmpty()) {
            // 如果有保存的邮箱和密码，执行自动登录
            executeLogin(email, password)
        } else {
            // 如果没有保存，显示登录对话框
            showLoginDialog()
        }
    }

    fun showLoginDialog() {
        val view = (context as Activity).layoutInflater.inflate(R.layout.dialog_login, null)
        dialog = DialogHelper.customDialog(context, view)
            .setCancelable(false)

        etEmail = view.findViewById(R.id.et_username)  // 改为 et_email
        etPassword = view.findViewById(R.id.et_password)

        setupButtons(view)
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "请输入邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            executeLogin(email, password)
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            val intent = Intent (context, ActionPageOnline::class.java)
            intent.putExtra("url", "https://rootes.top/user/register.php")
            context.startActivity(intent)

        }
    }

    fun executeLogin(email: String, password: String) {
        val url = "https://rootes.top/rootes/login.php" // 确保使用正确的协议（http/https）

        // 使用 OkHttp 进行网络请求
        val client = OkHttpClient()

        // 创建请求体
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("login", "true")
            .build()

        // 创建请求
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .addHeader("Accept", "application/json")
            .build()

        // 发送请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (context as Activity).runOnUiThread {
                    if (::dialog.isInitialized) {
                        dialog.dismiss()
                    }
                    showErrorToast("无法验证服务器")
                    showLoginDialog()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // 打印状态码和响应体
                val statusCode = response.code
                val responseBody = response.body?.string() ?: ""
                println("Response Code: $statusCode")
                println("Response Body: $responseBody")

                // 判断响应状态码是否为 200
                if (statusCode == 200) {
                    // 服务器成功响应，继续处理返回的数据
                    (context as Activity).runOnUiThread {
                        if (::dialog.isInitialized) {
                            dialog.dismiss()
                        }
                        handleLoginResponse(responseBody, email, password)
                    }
                } else {
                    // 如果不是 200，显示响应码和响应内容
                    (context as Activity).runOnUiThread {
                        if (::dialog.isInitialized) {
                            dialog.dismiss()
                        }
                        showErrorToast("服务器响应异常: 状态码 $statusCode")
                        showLoginDialog()
                    }
                }
            }
        })
    }

    private fun handleLoginResponse(responseData: String, email: String, password: String) {
        val jsonResponse = JSONObject(responseData)
        val status = jsonResponse.getString("status")
        val message = jsonResponse.getString("message")

        if (status == "success") {
            // 保存邮箱和密码到SharedPreferences
            SharedPreferencesHelper.saveUserCredentials(context, email, password)

            val resultIntent = Intent()
            resultIntent.putExtra("result", email)
            (context as Activity).setResult(Activity.RESULT_OK, resultIntent)

            Toast.makeText(context, "登入完毕", Toast.LENGTH_SHORT).show()

        } else {
            // 显示错误提示
            showErrorToast(message)

            // 如果密码或用户名错误，保留当前对话框
            if (message.contains("用户名或密码错误")) {
                showLoginDialog()  // 保持登录界面显示
            }
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
