package com.root.system.activities

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.root.system.R
import com.root.system.databinding.ActivtyModulesBinding
import com.root.ui.AdapterModules
import kotlinx.coroutines.*
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ActivityGithub : ActivityBase(), AdapterModules.OnItemClickListener {
    private lateinit var binding: ActivtyModulesBinding

    data class Module(val name: String, val url: String)

    private lateinit var downloadManager: DownloadManager
    private lateinit var downloadReceiver: BroadcastReceiver
    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var localContext: Context  // 修改此属性名

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivtyModulesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localContext = this // 初始化 localContext
        setBackArrow()
        onViewCreated()

        // 初始化下载管理器和广播接收器
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        setupDownloadReceivers()

        // 自动刷新并显示数据
        scope.launch {
            refreshData("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/lsp.json", "")
        }
    }

    private fun setupDownloadReceivers() {
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val action = it.action
                    val downloadId = it.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                        handleDownloadComplete(downloadId)
                    } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED == action) {
                        showToast("下载错误")
                    }
                }
            }
        }
        registerReceiver(downloadReceiver, IntentFilter().apply {
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
        scope.cancel()
    }

    private fun onViewCreated() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.moduleList.layoutManager = linearLayoutManager

        binding.moduleSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val text = v.text.toString()
                val view = (v as EditText)
                view.isEnabled = false
                scope.launch(Dispatchers.IO) {
                    try {
                        val modules = queryModulesFromUrl("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/lsp.json", text)
                        if (!isDestroyed) {
                            withContext(Dispatchers.Main) {
                                binding. moduleList.adapter = AdapterModules(this@ActivityGithub, ArrayList(modules.map { it.name })).apply {
                                    setOnItemClickListener(this@ActivityGithub)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        Log.e("ActivityModules", "Failed to find module", ex)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ActivityGithub, "无法连接服务器，请检查网络设置: ${ex.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        view.post {
                            view.isEnabled = true
                        }
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private suspend fun queryModulesFromUrl(url: String, keyword: String): List<Module> {
        return withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connect()

                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }

                val allModules = mutableListOf<Module>()
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val name = jsonObject.getString("name")
                    val moduleUrl = jsonObject.getString("url")
                    allModules.add(Module(name, moduleUrl))
                }

                allModules.filter { it.name.contains(keyword, ignoreCase = true) }
            } catch (e: Exception) {
                Log.e("ActivityModules", "Error querying modules from URL", e)
                emptyList()
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun refreshData(url: String, keyword: String) {
        try {
            val modules = queryModulesFromUrl(url, keyword)
            if (!isDestroyed) {
                withContext(Dispatchers.Main) {
                    binding.moduleList.adapter = AdapterModules(this@ActivityGithub, ArrayList(modules.map { it.name })).apply {
                        setOnItemClickListener(this@ActivityGithub)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("ActivityModules", "Failed to load modules", ex)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ActivityGithub, "无法连接服务器，请检查网络设置: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(view: View, position: Int) {
        scope.launch {
            try {
                val modules = queryModulesFromUrl("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/lsp.json", "")
                if (modules.isNotEmpty()) {
                    val module = modules[position]
                    downloadModule(module.url, module.name)
                } else {
                    Toast.makeText(this@ActivityGithub, "无法找到模块", Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                Log.e("ActivityModules", "Error on item click", ex)
                Toast.makeText(this@ActivityGithub, "下载模块失败: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadModule(url: String, moduleName: String) {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setAllowedOverRoaming(false)
            setTitle(moduleName)
            setDescription("正在下载 $moduleName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir("Download", "$moduleName.txt") // 使用 .txt 作为下载的文件名
        }
        val downloadId = downloadManager.enqueue(request)
    }

    private fun handleDownloadComplete(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriString = it.getString(it.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val uri = Uri.parse(uriString)
                    val file = File(uri.path!!)
                    val newFile = File(file.parent, file.name.replace(".txt", ".apk")) // 将 .txt 替换为 .zip
                    if (file.renameTo(newFile)) {
                        showToast("下载模块完成 ${newFile.name}，保存在/sdcard/Download")
                    } else {
                        showToast("重命名文件失败")
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    showToast("下载错误")
                }
            }
        }
        cursor?.close()
    }

    override fun onResume() {
        super.onResume()
        title = "应用商店"
    }
}
