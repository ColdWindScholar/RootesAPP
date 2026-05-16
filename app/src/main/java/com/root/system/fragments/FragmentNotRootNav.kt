package com.root.system.fragments

import android.content.Intent
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
import okhttp3.*
import java.io.IOException
import com.root.system.activities.ActivityCharge
import com.root.system.activities.ActivityPowerUtilization
import com.root.system.activities.ActivityTestColor

class FragmentNotRootNav : Fragment() {

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_nav2, container, false)

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.app_name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = view.findViewById(R.id.Service)

        fetchTextFromUrl("https://rootes.top/公告.txt", textView)

        nav_text.setOnClickListener {
            Toast.makeText(context, "🐮🍺", Toast.LENGTH_SHORT).show()
        }

        nav_otg.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "OTG功能（免Root）"
                pageConfigPath = "/data/data/com.root.system/files/usr/pages/OTGNoRoot.xml.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }
        
        nav_root.setOnClickListener {
            val intent = Intent(requireContext(), ActivityCharge::class.java)
            startActivity(intent)
        }

        nav_magisk.setOnClickListener {
            val intent = Intent(requireContext(), ActivityPowerUtilization::class.java)
            startActivity(intent)
        }

        nav_app.setOnClickListener {
            val intent = Intent(requireContext(), ActivityTestColor::class.java)
            startActivity(intent)
        }
    }

    private fun fetchTextFromUrl(url: String, textView: TextView) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
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
}
