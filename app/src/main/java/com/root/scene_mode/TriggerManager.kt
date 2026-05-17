package com.root.scene_mode

import android.content.Context
import com.root.model.TriggerInfo
import com.root.store.TriggerStorage
import androidx.core.content.edit

class TriggerManager(private var context: Context) {
    private val triggerListConfig = context.getSharedPreferences("scene_trigger_list", Context.MODE_PRIVATE)

    fun setTriggerAndSave(triggerInfo: TriggerInfo) {
        val events = StringBuilder()
        triggerInfo.events?.forEach {
            events.append(it.name)
            events.append(",")
        }

        triggerListConfig.edit { putString(triggerInfo.id, events.toString()) }
        TriggerStorage(context).save(triggerInfo)
    }

    fun removeTrigger(triggerInfo: TriggerInfo) {
        triggerListConfig.edit { remove(triggerInfo.id) }
        TriggerStorage(context).remove(triggerInfo.id)
    }

    fun list(): List<TriggerInfo?> {
        val storage = TriggerStorage(context)
        return triggerListConfig.all.keys.map {
            storage.load(it)
        }.filter { it != null }.toList()
    }
}