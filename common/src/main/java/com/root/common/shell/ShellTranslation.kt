package com.root.common.shell

import android.content.Context
import com.root.common.shared.ResourceStringResolver

// 从Resource解析字符串，实现输出内容多语言
class ShellTranslation(context: Context) : ResourceStringResolver(context)