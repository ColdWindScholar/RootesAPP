package com.root.krscript.ui

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.root.common.ui.DialogHelper
import com.root.krscript.R
import com.root.krscript.executor.ShellExecutor
import com.root.krscript.model.RunnableNode
import com.root.krscript.model.ShellHandlerBase
import kotlinx.android.synthetic.main.kr_dialog_log.*


class DialogLogFragment : androidx.fragment.app.DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // val view = inflater.inflate(R.layout.kr_dialog_log, container, false)

        currentView = inflater.inflate(R.layout.kr_dialog_log, container)
        return currentView
    }

    private var running = false
    private var nodeInfo: RunnableNode? = null
    private lateinit var onExit: Runnable
    private lateinit var script: String
    private var params: HashMap<String, String>? = null
    private var themeResId: Int = 0
    private lateinit var currentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity!!, if (themeResId != 0) themeResId else R.style.kr_full_screen_dialog_light)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = this.activity
        if (activity != null) {
            dialog?.window?.run {
                DialogHelper.setWindowBlurBg(this, activity)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (nodeInfo != null) {
            nodeInfo?.run {
                // 如果执行完以后需要刷新界面，那么就不允许隐藏日志窗口到后台执行
                if (reloadPage) {
                    btn_hide.visibility = View.GONE
                }

                val shellHandler = openExecutor(this)

                if (shellHandler != null) {
                    ShellExecutor().execute(activity, this, script, onExit, params, shellHandler)
                }
            }
        } else {
            dismiss()
        }
    }

    private fun openExecutor(nodeInfo: RunnableNode): ShellHandlerBase? {
        var forceStopRunnable: Runnable? = null

        btn_hide.setOnClickListener {
            closeView()
        }
        btn_exit.setOnClickListener {
            if (running) {
                forceStopRunnable?.run()
            }
            closeView()
        }

        btn_copy.setOnClickListener {
            try {
                val myClipboard: ClipboardManager = this.context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip: ClipData = ClipData.newPlainText("text", shell_output.text.toString())
                myClipboard.primaryClip = myClip
                Toast.makeText(context, getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(context, getString(R.string.copy_fail), Toast.LENGTH_SHORT).show()
            }
        }
        if (nodeInfo.interruptable) {
            btn_hide?.visibility = View.VISIBLE
            btn_exit?.visibility = View.VISIBLE
        } else {
            btn_hide?.visibility = View.GONE
            btn_exit?.visibility = View.GONE
        }

        if (!nodeInfo.title.isEmpty()) {
            title.text = nodeInfo.title
        } else {
            title.visibility = View.GONE
        }

        if (!nodeInfo.desc.isEmpty()) {
            desc.text = nodeInfo.desc
        } else {
            desc.visibility = View.GONE
        }

        action_progress.isIndeterminate = true
        return MyShellHandler(object : IActionEventHandler {
            override fun onCompleted() {
                running = false

                onExit.run()
                if (btn_hide != null) {
                    btn_hide.visibility = View.GONE
                    btn_exit.visibility = View.VISIBLE
                    action_progress.visibility = View.GONE
                }

                isCancelable = true
            }

            override fun onSuccess() {
                if (nodeInfo.autoOff) {
                    closeView()
                }
            }

            override fun onStart(forceStop: Runnable?) {
                running = true

                if (nodeInfo.interruptable && forceStop != null) {
                    btn_exit.visibility = View.VISIBLE
                } else {
                    btn_exit.visibility = View.GONE
                }
                forceStopRunnable = forceStop
            }

        }, shell_output, action_progress)
    }

    @FunctionalInterface
    interface IActionEventHandler {
        fun onStart(forceStop: Runnable?)
        fun onSuccess()
        fun onCompleted()
    }

    class MyShellHandler(
            private var actionEventHandler: IActionEventHandler,
            private var logView: TextView,
            private var shellProgress: ProgressBar) : ShellHandlerBase() {



        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EVENT_EXIT -> onExit(msg.obj)
                EVENT_START -> {
                    onStart(msg.obj)
                }
                EVENT_READ -> onWrite(msg.obj)
                EVENT_READ_ERROR -> onError(msg.obj)
                EVENT_WRITE -> {
                    onWrite(msg.obj)
                }
            }
        }


        override fun onStart(msg: Any?) {
            this.logView.text = ""
            // updateLog(msg, scriptColor)
        }

        override fun updateLog(msg: SpannableString?) {
            if (msg != null) {
                this.logView.post {
                    logView.append(msg)
                    (logView.parent as ScrollView).fullScroll(ScrollView.FOCUS_DOWN)
                }
            }
        }
    }

    private fun closeView() {
        try {
            dismiss()
        } catch (ex: java.lang.Exception) {
        }
    }

    private var onDismissRunnable: Runnable? = null
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissRunnable?.run()
        onDismissRunnable = null
    }

    companion object {
        fun create(nodeInfo: RunnableNode,
                   onExit: Runnable,
                   onDismiss: Runnable,
                   script: String,
                   params: HashMap<String, String>?,
                   darkMode: Boolean = false): DialogLogFragment {
            val fragment = DialogLogFragment()
            fragment.nodeInfo = nodeInfo
            fragment.onExit = onExit
            fragment.script = script
            fragment.params = params
            fragment.themeResId = if (darkMode) R.style.kr_full_screen_dialog_dark else R.style.kr_full_screen_dialog_light
            fragment.onDismissRunnable = onDismiss
            return fragment
        }
    }
}
