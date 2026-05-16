package com.root.system.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.root.permissions.CheckRootStatus
import com.root.system.databinding.FragmentNotRootBinding


class FragmentNotRoot : androidx.fragment.app.Fragment() {
    private lateinit var binding: FragmentNotRootBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentNotRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnRetry.setOnClickListener {
            CheckRootStatus(this.requireContext(), {
                if (this.activity != null) {
                    this.requireActivity().recreate()
                }
            }, false, null).forceGetRoot()
        }
    }

    companion object {
        fun createPage(): androidx.fragment.app.Fragment {
            val fragment = FragmentNotRoot()
            return fragment
        }
    }
}
