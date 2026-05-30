package com.root.system.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.root.system.R
import com.root.system.databinding.FragmentDonateBinding
import com.root.system.dialogs.DialogWX

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



        binding.payWxpay2.setOnClickListener {
          val dialogWX = DialogWX(requireActivity())
            dialogWX.showWXMenu()
        }

    }

}
