package com.root.system.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.root.common.ui.DialogHelper
import com.root.system.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

class DiagRegister(private val context: Context) {
    private lateinit var dialog: DialogHelper.DialogWrap
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etCode: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSendCode: Button

    // 显示注册对话框
    fun showRegisterDialog() {
        val view = (context as Activity).layoutInflater.inflate(R.layout.dialog_register, null)
        dialog = DialogHelper.customDialog(context, view).setCancelable(false)

        etUsername = view.findViewById(R.id.et_username)
        etEmail = view.findViewById(R.id.et_email)
        etCode = view.findViewById(R.id.et_code)
        etPassword = view.findViewById(R.id.et_password)
        btnSendCode = view.findViewById(R.id.btn_send_code)

        setupButtons(view)
    }

    private fun setupButtons(view: View) {
        // 发送验证码按钮
        btnSendCode.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("请输入有效邮箱")
                return@setOnClickListener
            }
            sendVerificationCode(email)
        }

        // 注册按钮
        view.findViewById<Button>(R.id.btn_register).setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val code = etCode.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                username.isEmpty() -> showToast("请输入用户名")
                !isValidUsername(username) -> showToast("用户名格式不正确")
                email.isEmpty() -> showToast("请输入邮箱")
                code.isEmpty() -> showToast("请输入验证码")
                password.isEmpty() -> showToast("请输入密码")
                password.length < 6 -> showToast("密码至少6位")
                else -> executeRegister(username, email, code, password)
            }
        }
        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            val dialogLogin = DiagLogin(context)
            dialogLogin.showLoginDialog()

        }

    }

    // 发送验证码
    private fun sendVerificationCode(email: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        val formBody = FormBody.Builder()
            .add("action", "send_code")
            .add("email", email)
            .build()

        val request = Request.Builder()
            .url("https://rootes.top/user/register2.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    showToast("网络连接失败")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                (context as Activity).runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        if (json.getBoolean("success")) {
                            startCountdown()
                            showToast("验证码已发送")
                        } else {
                            showToast(json.getString("message"))
                        }
                    } catch (e: Exception) {
                        showToast("验证码发送失败")
                    }
                }
            }
        })
    }

    // 执行注册
    private fun executeRegister(username: String, email: String, code: String, password: String) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()

        val formBody = FormBody.Builder()
            .add("action", "register")
            .add("username", username)
            .add("email", email)
            .add("code", code)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("https://rootes.top/rootes/register2.php")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    dialog.dismiss()
                    showToast("网络连接失败")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string() ?: ""
                (context as Activity).runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        if (json.getBoolean("success")) {
                            onRegisterSuccess()
                        } else {
                            showToast(json.getString("message"))
                        }
                    } catch (e: Exception) {
                        showToast("注册解析失败")
                    }
                    dialog.dismiss()
                }
            }
        })
    }

    // 用户名格式验证（2-16位，支持中文、字母、数字和下划线）
    private fun isValidUsername(username: String): Boolean {
        val regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,16}$".toRegex()
        return regex.matches(username)
    }

    // 注册成功处理
    // 修改注册成功处理部分
    private fun onRegisterSuccess() {
        showToast("注册成功，需要购买玩机百宝箱 服务器费用")
        (context as Activity).setResult(Activity.RESULT_OK)
        dialog.dismiss()
        val diagLogin = DiagLogin(context)
        diagLogin.showLoginDialog()
            context.startActivity(Intent(Intent.ACTION_VIEW, "https://rootes.top/".toUri()))
            Toast.makeText(context, "请到官方网站注册", Toast.LENGTH_SHORT).show()

            val dialogLogin = DiagLogin(context)
            dialogLogin.showLoginDialog()


    }

    // 倒计时处理
    private fun startCountdown() {
        btnSendCode.isEnabled = false
        val totalTime = 60
        object : CountDownTimer(totalTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                btnSendCode.text = "${millisUntilFinished / 1000}秒后重试"
            }

            override fun onFinish() {
                btnSendCode.isEnabled = true
                btnSendCode.text = "发送验证码"
            }
        }.start()
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    abstract class CountDownTimer(millisInFuture: Long, countDownInterval: Long) {
        abstract fun onTick(millisUntilFinished: Long)
        abstract fun onFinish()
        fun start() {}
    }
}