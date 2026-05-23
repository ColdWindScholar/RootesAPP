package com.root.system.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.root.common.ui.DialogHelper
import com.root.system.R
import com.root.system.databinding.FragmentDonateBinding
import com.root.system.dialogs.DialogWX
import java.io.File
import kotlin.system.exitProcess

class FragmentDonate : Fragment() {
    private lateinit var binding: FragmentDonateBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = getString(R.string.app_name)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Load user data from SharedPreferences

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

    }

}
