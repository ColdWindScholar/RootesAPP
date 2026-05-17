package com.root.utils

import android.app.Activity
import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import com.root.common.ui.DialogHelper
import com.root.common.ui.ProgressBarDialog

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
            context.runOnUiThread {
                // progressBarDialog.dismissDialog()

                progressBarDialog.hideDialog()
                DialogHelper.alert(context, "提示", "无法加载内容，请检查网络连接。")
            }
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            response.body.let { responseBody ->
                val content = responseBody.string()
                context.runOnUiThread {
                    //  progressBarDialog.dismissDialog()

                    progressBarDialog.hideDialog()
                    DialogHelper.alert(context, "提示", content)
                }
            }
        }
    })
}
