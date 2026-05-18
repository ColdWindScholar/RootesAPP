package com.root.store

import android.content.Context
import com.root.common.shared.ObjectStorage
import com.root.model.TriggerInfo

class TriggerStorage(private val context: Context) : ObjectStorage<TriggerInfo>(context) {

    fun save(obj: TriggerInfo): Boolean {
        return super.save(obj, obj.id)
    }

}
