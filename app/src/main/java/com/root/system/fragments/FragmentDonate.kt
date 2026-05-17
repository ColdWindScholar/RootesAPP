package com.root.system.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.DialogHelper
import com.root.system.R
import com.root.system.databinding.FragmentDonateBinding
import com.root.system.dialogs.DialogWX
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class FragmentDonate : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var binding: FragmentDonateBinding
    private lateinit var emailTextView: TextView
    private lateinit var joinedAtTextView: TextView

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = getString(R.string.app_name)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the views
        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        joinedAtTextView = view.findViewById(R.id.joinedAtTextView)

        // Load user data from SharedPreferences
        loadUserData()

        binding.payWxpay.setOnClickListener {
            DialogHelper.confirm(
                requireActivity(),
                "是否确定选择操作？",
                onConfirm = DialogHelper.DialogButton("执行操作", {
                    File("/data/data/com.root.system/shared_prefs/user_prefs.xml").delete()
                    exitProcess(0)
                }),
                onCancel = DialogHelper.DialogButton("取消执行")
            )
            }

        binding.payWxpay2.setOnClickListener {
          val dialogWX = DialogWX(requireActivity())
            dialogWX.showWXMenu()
        }
    
    

        // Trigger login when the data is loaded
        val email = emailTextView.text.toString().removePrefix("邮箱: ")
        val password = getPasswordFromSharedPreferences()

        if (email != "No email found" && password != "No password found") {
            login(email, password)
        } else {
            Toast.makeText(context, "未找到有效的邮箱和密码", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        // Access SharedPreferences
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

        // Retrieve stored email
        val email = sharedPreferences.getString("user_email", "No email found")
        val password = sharedPreferences.getString("user_password", "No password found")

        // Display the user data on the TextViews
        emailTextView.text = "邮箱: $email 密码:$password"
    }

    private fun getPasswordFromSharedPreferences(): String {
        // Access SharedPreferences to retrieve password
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_password", "No password found") ?: "No password found"
    }

    private fun login(email: String, password: String) {
        // Prepare POST request body
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        // Create the POST request
        val request = Request.Builder()
            .url("http://rootes.top/rootes/login.php")
            .post(formBody)
            .build()

        // Make the network request on a background thread
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle request failure
                activity?.runOnUiThread {
                    Toast.makeText(context, "登录请求失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val status = jsonResponse.getString("status")

                        if (status == "success") {
                            // Retrieve and display only the username, email, and joined_at
                            val username = jsonResponse.getString("username")
                            val email = jsonResponse.getString("email")
                            val joinedAt = jsonResponse.getString("joined_at")

                            // Update the UI with the user information
                            activity?.runOnUiThread {
                                usernameTextView.text = "用户名: $username"
                                emailTextView.text = "邮箱: $email"
                                joinedAtTextView.text = "注册时间: $joinedAt"
                            }
                        } else {
                            // Login failed, show Toast and trigger app crash
                            activity?.runOnUiThread {
                                Toast.makeText(context, "登录失败: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                                throw Exception("登录失败，错误信息: ${jsonResponse.getString("message")}")
                            }
                        }
                    } catch (e: Exception) {
                        // JSON parsing failure or other errors
                        activity?.runOnUiThread {
                            Toast.makeText(context, "解析失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            throw e  // Throw an exception to trigger crash
                        }
                    }
                } else {
                    // Request failed, show Toast and trigger app crash
                    activity?.runOnUiThread {
                        Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show()
                        throw Exception("请求失败")
                    }
                }
            }
        })
    }
}
