package com.root.system.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.CheckBox
import android.widget.HeaderViewListAdapter
import android.widget.Toast
import com.root.Scene
import com.root.common.ui.OverScrollListView
import com.root.common.ui.ProgressBarDialog
import com.root.model.AppInfo
import com.root.ui.AdapterAppList
import com.root.utils.AppListHelper
import com.root.system.R
import com.root.system.databinding.FragmentAppListBinding
import com.root.system.dialogs.DialogAppOptions
import com.root.system.dialogs.DialogSingleAppOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class FragmentAppBackup(private val myHandler: Handler) : androidx.fragment.app.Fragment() {
    private lateinit var processBarDialog: ProgressBarDialog
    private lateinit var appListHelper: AppListHelper
    private var appList: ArrayList<AppInfo>? = null
    private var keywords = ""
    private lateinit var binding: FragmentAppListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        processBarDialog = ProgressBarDialog(requireActivity(), "FragmentAppBackup")
        appListHelper = AppListHelper(requireContext())
        binding = FragmentAppListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appList.addHeaderView(this.layoutInflater.inflate(R.layout.list_header_app, null))

        val onItemLongClick = AdapterView.OnItemLongClickListener { parent, _, position, id ->
            if (position < 1)
                return@OnItemLongClickListener true
            val adapter = (parent.adapter as HeaderViewListAdapter).wrappedAdapter
            val app = adapter.getItem(position - 1) as AppInfo
            DialogSingleAppOptions(requireActivity(), app, myHandler!!).showSingleAppOptions()
            true
        }

        binding.appList.onItemLongClickListener = onItemLongClick
        binding.fabApps.setOnClickListener {
            getSelectedAppShowOptions(requireActivity())
        }

        this.setList()
    }

    private fun getSelectedAppShowOptions(activity: Activity) {
        var adapter = binding.appList.adapter
        adapter = (adapter as HeaderViewListAdapter).wrappedAdapter
        val selectedItems = (adapter as AdapterAppList).getSelectedItems()
        if (selectedItems.size == 0) {
            Scene.toast(R.string.app_selected_none, Toast.LENGTH_SHORT)
            return
        }

        DialogAppOptions(activity, selectedItems, myHandler).selectBackupOptions()
    }

    private fun setList() {
        processBarDialog.showDialog()
        GlobalScope.launch(Dispatchers.Main) {
            appList = appListHelper.getShadowAppList()
            processBarDialog.hideDialog()
            binding.appList?.run {
                setListData(appList, this)
            }
        }
    }

    private fun setListData(dl: ArrayList<AppInfo>?, lv: OverScrollListView) {
        if (dl == null)
            return
        myHandler.post {
            try {
                val adapterObj = AdapterAppList(requireContext(), dl, keywords)
                val adapterAppList: WeakReference<AdapterAppList> = WeakReference(adapterObj)
                lv.adapter = adapterObj
                lv.onItemClickListener = OnItemClickListener { list, itemView, postion, _ ->
                    if (postion == 0) {
                        val checkBox = itemView.findViewById(R.id.select_state_all) as CheckBox
                        checkBox.isChecked = !checkBox.isChecked
                        if (adapterAppList.get() != null) {
                            adapterAppList.get()!!.setSelecteStateAll(checkBox.isChecked)
                            adapterAppList.get()!!.notifyDataSetChanged()
                        }
                    } else {
                        val checkBox = itemView.findViewById(R.id.select_state) as CheckBox
                        checkBox.isChecked = !checkBox.isChecked
                        val all = lv.findViewById<CheckBox>(R.id.select_state_all)
                        if (adapterAppList.get() != null) {
                            all.isChecked = adapterAppList.get()!!.getIsAllSelected()
                        }
                    }
                    binding.fabApps.visibility = if (adapterAppList.get()?.hasSelected() == true) View.VISIBLE else View.GONE
                }
                val all = lv.findViewById<CheckBox>(R.id.select_state_all)
                all.isChecked = false
                binding.fabApps.visibility = View.GONE
            } catch (ex: Exception) {
            }
        }
    }

    var searchText: String
        get () {
            return keywords
        }
        set (value) {
            if (keywords != value) {
                keywords = value
                binding.appList?.run {
                    setListData(appList, this)
                }
            }
        }

    fun reloadList() {
        setList()
    }
}
