package com.root.system.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.projectkr.shell.OpenPageHelper
import com.root.krscript.model.PageNode
import com.root.system.R
import com.root.system.activities.ActivityCharge
import com.root.system.activities.ActivityPowerUtilization
import com.root.system.activities.ActivityTestColor
import com.root.system.databinding.FragmentNav2Binding
import okhttp3.*
import java.io.IOException

class FragmentNotRootNav : Fragment() {

    private val client = OkHttpClient()
    private lateinit var binding: FragmentNav2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNav2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.app_name)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textView: TextView = view.findViewById(R.id.Service)

        fetchTextFromUrl("https://github.com/ColdWindScholar/RootEsFiles/raw/refs/heads/main/notice.txt", textView)

        binding.navOtg.setOnClickListener {
            val pageNode = PageNode("").apply {
                title = "OTG功能（免Root）"
                pageConfigPath = "${context?.filesDir}/usr/pages/OTGNoRoot.xml.xml"
            }
            OpenPageHelper(requireActivity()).openPage(pageNode)
        }

        binding.navRoot.setOnClickListener {
            val intent = Intent(requireContext(), ActivityCharge::class.java)
            startActivity(intent)
        }

        binding.navMagisk.setOnClickListener {
            val intent = Intent(requireContext(), ActivityPowerUtilization::class.java)
            startActivity(intent)
        }

        binding.navApp.setOnClickListener {
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
                        val responseData = response.body.string()
                        activity?.runOnUiThread {
                            textView.text = responseData
                        }
                    }
                }
            }
        })
    }
}
