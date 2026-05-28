package com.root.system.activities

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.root.Scene
import com.root.common.shell.KeepShellPublic
import com.root.common.ui.ThemeMode
import com.root.system.R

open class ActivityBase : AppCompatActivity() {
    lateinit var themeMode: ThemeMode
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        this.themeMode = ThemeSwitch.switchTheme(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.themeMode = ThemeSwitch.switchTheme(this)
    }

    protected val context: Context
        get() {
            return this
        }

    protected fun setBackArrow() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // 显示返回按钮
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            this.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Scene.postDelayed({
            System.gc()
        }, 500)
        if (isTaskRoot) {
            Scene.postDelayed({
                KeepShellPublic.doCmdSync("dumpsys meminfo ${context.packageName}")
            }, 100)
        }
    }
}