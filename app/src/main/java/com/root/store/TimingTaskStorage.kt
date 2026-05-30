package com.root.store

import android.content.Context
import com.root.common.shared.ObjectStorage
import com.root.model.TimingTaskInfo

class TimingTaskStorage(context: Context) : ObjectStorage<TimingTaskInfo>(context) {

    fun save(obj: TimingTaskInfo): Boolean {
        return super.save(obj, obj.taskId)
    }

}
