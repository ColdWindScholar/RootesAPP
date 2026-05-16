package com.root.system.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.root.system.BuildConfig
import com.root.system.R

class ActivityAbout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // 获取控件
        val compileTimeTextView: TextView = findViewById(R.id.compile_time)
        val compileHostTextView: TextView = findViewById(R.id.compile_host)
        val compileProcessorTextView: TextView = findViewById(R.id.compile_processor)
        val compileSystemTextView: TextView = findViewById(R.id.compile_system)

        // 设置显示信息
        compileTimeTextView.text = "版本号: ${BuildConfig.VERSION_CODE}"
        compileHostTextView.text = "调试模式: ${BuildConfig.DEBUG}"
        compileProcessorTextView.text = "软件ID: ${BuildConfig.APPLICATION_ID}"
        compileSystemTextView.text = "编译类型: ${BuildConfig.BUILD_TYPE}"
    }
}
