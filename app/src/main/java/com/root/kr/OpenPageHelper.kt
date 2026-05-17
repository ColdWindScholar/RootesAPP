package com.projectkr.shell

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.root.common.ui.DialogHelper
import com.root.common.ui.ProgressBarDialog
import com.root.krscript.model.PageNode
import com.root.system.activities.ActionPage
import com.root.system.activities.ActionPageOnline
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class OpenPageHelper(private var activity: Activity) {
    private var progressBarDialog: ProgressBarDialog? = null
    private var handler = Handler(Looper.getMainLooper())

    private val dialog: ProgressBarDialog
        get() {
            if (progressBarDialog == null) {
                progressBarDialog = ProgressBarDialog(activity)
            }
            return progressBarDialog!!
        }

    private fun showDialog(msg: String) {
        handler.post {
            dialog.showDialog(msg)
        }
    }

    private fun hideDialog() {
        handler.post {
            dialog.hideDialog()
        }
    }

    fun openPage(pageNode: PageNode) {
        try {
            var intent: Intent? = null

            if (pageNode.onlineHtml2Page.isNotEmpty()) {
                // 调用 showUpdateDialog，传递 Context 和 Intent
                showUpdateDialog(activity)
            }

            // 如果 onlineHtml2Page 不为空，启动 ActivityAddinOnline
            if (pageNode.onlineHtmlPage.isNotEmpty()) {
                intent = Intent(activity, ActionPageOnline::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("url", pageNode.onlineHtmlPage)
                }
            }


            if (!pageNode.pageConfigSh.isEmpty()) {
                if (intent == null) {
                    intent = Intent(activity, ActionPage::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (!pageNode.pageConfigPath.isEmpty()) {
                if (intent == null) {
                    intent = Intent(activity, ActionPage::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            intent?.run {
                intent.putExtra("page", pageNode)
                activity.startActivity(intent)
            }
        } catch (ex: Exception) {
            Toast.makeText(activity, "" + ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showUpdateDialog(context: Context) {
        val activity = context as? Activity ?: return

        val progressBarDialog = ProgressBarDialog(activity)

        progressBarDialog.showDialog("正在连接服务器")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/about.txt")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                context?.runOnUiThread {
                    // progressBarDialog.dismissDialog()

                    progressBarDialog.hideDialog()
                    DialogHelper.alert(context, "提示", "无法加载内容，请检查网络连接。")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.body?.let { responseBody ->
                    val content = responseBody.string()
                    (context as? Activity)?.runOnUiThread {
                        //  progressBarDialog.dismissDialog()

                        progressBarDialog.hideDialog()
                        DialogHelper.alert(context, "提示", content)
                    }
                }
            }
        })
    }
}