package com.root.system.activities

import android.os.Bundle
import android.widget.Toast
import com.root.common.shared.FileWrite
import com.root.common.shared.MagiskExtend
import com.root.common.shared.KernelSUExtend
import com.root.common.shared.RootFileInfo
import com.root.common.shell.RootFile
import com.root.common.ui.DialogHelper
import com.root.common.ui.ProgressBarDialog
import com.root.system.ModuleInstaller
import com.root.ui.AdapterRootFileSelector
import com.root.system.R
import com.root.system.databinding.ActivityMagiskBinding
import java.io.File


class ActivityMagisk : ActivityBase() {
    private lateinit var binding: ActivityMagiskBinding
    private var adapterFileSelector: AdapterRootFileSelector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMagiskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setBackArrow()

        onViewCreated()
    }

    override fun onResume() {
        super.onResume()
        title = getString(R.string.menu_app_magisk)
    }

    fun onViewCreated() {
        if (MagiskExtend.magiskSupported()) {
            if (!MagiskExtend.moduleInstalled()) {
                DialogHelper.confirm(this, "安装Magisk拓展？",
                        "安装拓展模块，从而在不修改系统文件的情况下，更改一些参数~",
                        {
                            MagiskExtend.magiskModuleInstall(context)
                            Toast.makeText(context, "操作已执行~", Toast.LENGTH_LONG).show()
                            this@ActivityMagisk.recreate()
                        })
            }
        } else {
            val zipFilePath = "/data/data/com.root.system/files/startboot.zip"
            ModuleInstaller.installModule(context, zipFilePath)

        if (!KernelSUExtend.moduleInstalled()) {
                DialogHelper.confirm(this, "安装Magisk拓展？",
                        "安装拓展模块，从而在不修改系统文件的情况下，更改一些参数~",
                        {
                            KernelSUExtend.magiskModuleInstall(context)
                            Toast.makeText(context, "操作已执行~", Toast.LENGTH_LONG).show()
                            this@ActivityMagisk.recreate()
                        })
            }
            return
        }

        binding.magiskTabhost.setup()

        binding.magiskTabhost.addTab(binding.magiskTabhost.newTabSpec("system.prop").setContent(R.id.magisk_tab1).setIndicator("属性"))
        binding.magiskTabhost.addTab(binding.magiskTabhost.newTabSpec("system.file").setContent(R.id.magisk_tab2).setIndicator("系统文件"))
        binding.magiskTabhost.addTab(binding.magiskTabhost.newTabSpec("before_start").setContent(R.id.magisk_tab3).setIndicator("启动前"))
        binding.magiskTabhost.addTab(binding.magiskTabhost.newTabSpec("after_start").setContent(R.id.magisk_tab4).setIndicator("启动后"))
        binding.magiskTabhost.currentTab = 0

        binding.magiskProps.setText(MagiskExtend.getProps())
        binding.magiskPropsReset.setOnClickListener {
            binding.magiskProps.setText(MagiskExtend.getProps())
        }
        binding. magiskPropsSave.setOnClickListener {
            if (FileWrite.writePrivateFile((binding.magiskProps.text.toString() + "\n").toByteArray(), "magisk_system.prop", context)) {
                val file = FileWrite.getPrivateFilePath(context, "magisk_system.prop")
                if (MagiskExtend.updateProps(file)) {
                    binding.magiskProps.setText(MagiskExtend.getProps())
                    Toast.makeText(context, "已保存更改，重启后生效 ^_~ ", Toast.LENGTH_LONG).show()
                    File(file).delete()
                } else {
                    Toast.makeText(context, "Magisk镜像空间不足，操作失败！~", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "保存失败!_*", Toast.LENGTH_LONG).show()
            }
        }


        binding.magiskBeforestart.setText(MagiskExtend.getFsPostDataSH())
        binding.magiskBeforestartReset.setOnClickListener {
            binding.magiskBeforestart.setText(MagiskExtend.getFsPostDataSH())
        }
        binding.magiskBeforestartSave.setOnClickListener {
            if (FileWrite.writePrivateFile((binding.magiskBeforestart.text.toString() + "\n").toByteArray(), "magisk_post-fs-data.sh", context)) {
                val file = FileWrite.getPrivateFilePath(context, "magisk_post-fs-data.sh")
                if (MagiskExtend.updateFsPostDataSH(file)) {
                    binding.magiskBeforestart.setText(MagiskExtend.getFsPostDataSH())
                    Toast.makeText(context, "已保存更改，重启后生效 ^_~ ", Toast.LENGTH_LONG).show()
                    File(file).delete()
                } else {
                    Toast.makeText(context, "Magisk镜像空间不足，操作失败！~", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "保存失败!_*", Toast.LENGTH_LONG).show()
            }
        }


        binding.magiskAfterstart.setText(MagiskExtend.getServiceSH())
        binding. magiskAfterstartReset.setOnClickListener {
            binding.magiskAfterstart.setText(MagiskExtend.getServiceSH())
        }
        binding.magiskAfterstartSave.setOnClickListener {
            if (FileWrite.writePrivateFile((binding.magiskAfterstart.text.toString() + "\n").toByteArray(), "magisk_service.sh", context)) {
                val file = FileWrite.getPrivateFilePath(context, "magisk_service.sh")
                if (MagiskExtend.updateServiceSH(file)) {
                    binding.magiskAfterstart.setText(MagiskExtend.getServiceSH())
                    Toast.makeText(context, "已保存更改，重启后生效 ^_~ ", Toast.LENGTH_LONG).show()
                    File(file).delete()
                } else {
                    Toast.makeText(context, "Magisk镜像空间不足，操作失败！~", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "保存失败!_*", Toast.LENGTH_LONG).show()
            }
        }
        adapterFileSelector = AdapterRootFileSelector(RootFileInfo(MagiskExtend.MAGISK_PATH + "system"), {
            val file: RootFileInfo? = adapterFileSelector!!.selectedFile
        }, ProgressBarDialog(this), null, false, true, {
            val file: RootFileInfo? = adapterFileSelector!!.selectedFile
            if (file != null) {
                RootFile.deleteDirOrFile(file.absolutePath)
                adapterFileSelector!!.refresh()
            }
        }, false)
        binding.magiskFiles.adapter = adapterFileSelector
    }
}
