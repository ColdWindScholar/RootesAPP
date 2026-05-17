package com.root.system.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.projectkr.shell.OpenPageHelper
import com.root.krscript.model.PageNode
import com.root.system.R
import com.root.system.databinding.FragmentNavBinding
import okhttp3.*
import java.io.IOException

class FragmentNav : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentNavBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding =  FragmentNavBinding.inflate(layoutInflater,container,false)
    return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.app_name)
    }
    private val client = OkHttpClient()

    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = view.findViewById(R.id.Service)
        val textView2: TextView = view.findViewById(R.id.Service2)


        fetchTextFromUrl("https://v1.jinrishici.com/rensheng.txt", textView2)

        fetchTextFromUrl("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/notice.txt", textView)



        binding.navOtg.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "OTG功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/OTG.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navMagisk.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "Magisk功能"
                pageConfigPath = "/data/data/com.root.system/files/usr/pages/Magisk.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navRoot.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "Root功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/Root.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navApp.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "软件功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/APP.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding. navSystem.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "系统功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/System.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navBattery.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "电池功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/battery.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navFq.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "分区功能"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/fq.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navBm.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "显示功能"
                pageConfigPath = "/data/data/com.root.system/files/usr/pages/Home/pm.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding. navDownload.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "资源中心"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/download.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }
        binding.navText2.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "公告"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/rootes.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navData.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "文件功能"
                pageConfigPath = "/data/data/com.root.system/files/usr/pages/Home/files.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navHelpabout.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "搜索"
                pageConfigSh = "/data/data/com.root.system/files/usr/pages/Home/about.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }
    }
private fun fetchTextFromUrl(url: String, textView: TextView) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 在UI线程中处理错误
                activity?.runOnUiThread {
                    textView.text = "网络请求失败: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        activity?.runOnUiThread {
                            textView.text = "网络请求失败: ${response.message}"
                        }
                    } else {
                        val responseData = response.body?.string()
                        activity?.runOnUiThread {
                            textView.text = responseData ?: "没有收到数据"
                        }
                    }
                }
            }
        })
    }
    override fun onClick(v: View?) {
        // Handle generic click events here if needed
    }
}
