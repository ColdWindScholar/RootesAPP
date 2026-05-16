package com.root.system.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.root.common.ui.DialogHelper
import com.root.store.SpfConfig
import com.root.system.R
import com.root.system.addin.DexCompileAddin
import com.root.system.addin.Immersive
import com.root.system.databinding.ActivityAddinBinding
import com.root.system.dialogs.DialogAddinModifyDPI
import com.root.system.dialogs.DialogAddinModifyDevice
import com.root.system.dialogs.DialogAddinWIFI
import com.root.system.dialogs.DialogCustomMAC
import java.util.*

class ActivityAddin : ActivityBase() {
    private lateinit var binding: ActivityAddinBinding
    private fun createItem(title: String, desc: String, runnable: Runnable?, wran: Boolean = true): HashMap<String, Any> {
        val item = HashMap<String, Any>()
        item["Title"] = title
        item["Desc"] = desc
        item["Wran"] = wran
        if (runnable != null)
            item["Action"] = runnable
        return item
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBackArrow()

        initAddin(binding.addinSystemListview)
    }

    private fun initAddin(view: View) {
        val activity = this
        val context = this
        val listItem = ArrayList<HashMap<String, Any>>().apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                add(createItem(getString(R.string.addin_fullscreen_on), getString(R.string.addin_fullscreen_on_desc), { Immersive(activity).fullScreen() }, false))
            }

            add(createItem(getString(R.string.addin_wifi), getString(R.string.addin_wifi_desc), { DialogAddinWIFI(context).show() }, false))

            add(createItem(getString(R.string.addin_dpi), getString(R.string.addin_dpi_desc), { DialogAddinModifyDPI(context).modifyDPI(activity.windowManager.defaultDisplay, context) }, false))

            add(createItem(getString(R.string.addin_deviceinfo), getString(
                    R.string.addin_deviceinfo_desc),
                    {
                        DialogAddinModifyDevice(context).modifyDeviceInfo()
                    },
                    false))
            add(createItem(getString(R.string.addin_mac),
                    getString(R.string.addin_mac_desc),
                    {
                        DialogCustomMAC(context).modifyMAC(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_1)
                    },
                    false))
            add(createItem(getString(R.string.addin_mac_2),
                    getString(R.string.addin_mac_desc_2),
                    {
                        DialogCustomMAC(context).modifyMAC(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_2)
                    },
                    false))

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                add(createItem(getString(R.string.addin_force_dex_compile), getString(R.string.addin_force_dex_compile_desc), { DexCompileAddin(context).run() }, false))
            }
            /*
            add(createItem(getString(R.string.addin_pm_dexopt), getString(R.string.addin_pm_dexopt_desc), Runnable { DexCompileAddin(context).modifyConfig() }, false))
            */
        }

        val mSimpleAdapter = SimpleAdapter(
                view.context, listItem,
                R.layout.list_item_action,
                arrayOf("Title", "Desc"),
                intArrayOf(R.id.Title, R.id.Desc)
        )
        binding.addinSystemListview.adapter = mSimpleAdapter
        binding.addinSystemListview.onItemClickListener = onActionClick
    }

    private var onActionClick = AdapterView.OnItemClickListener { parent, _, position, _ ->
        val item = parent.adapter.getItem(position) as HashMap<*, *>
        if (item.get("Wran") == false) {
            (item["Action"] as Runnable).run()
        } else {
            DialogHelper.confirm(this,
                    item["Title"].toString(),
                    item["Desc"].toString(), {
                (item["Action"] as Runnable).run()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        title = getString(R.string.menu_sundry)
    }
}
