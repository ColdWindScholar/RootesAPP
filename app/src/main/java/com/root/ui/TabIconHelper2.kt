package com.root.ui

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.root.system.R

class TabIconHelper2(
    private var tabLayout: TabLayout,
    private var viewPager: ViewPager,
    private var activity: Activity,
    private val supportFragmentManager: FragmentManager,
    private var layout: Int = R.layout.list_item_tab
) {
    private val fragments = ArrayList<Fragment>()
    private var views = ArrayList<View>()
    private var tabsInited = false

    val adapter = object : FragmentPagerAdapter(supportFragmentManager) {
        override fun getCount(): Int = fragments.size

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getPageTitle(position: Int): CharSequence = ""
    }

    fun newTabSpec(tabText: String, tabIcon: Drawable, fragment: Fragment): String {
        val layout = View.inflate(activity, layout, null)
        val imageView = layout.findViewById<ImageView>(R.id.ItemIcon)
        val textView = layout.findViewById<TextView>(R.id.ItemTitle)
        val tabId = "tab_${views.size}"

        textView.text = tabText
        imageView.setImageDrawable(tabIcon)
        if (views.isNotEmpty()) layout.alpha = 0.3f

        views.add(layout)
        fragments.add(fragment)
        tabLayout.addTab(tabLayout.newTab().setCustomView(layout)) // 添加 Tab
        adapter.notifyDataSetChanged()

        tabsInited = false
        return tabId
    }

    private fun getTabIndex(tabText: String): Int {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val textView = tab?.customView?.findViewById<TextView>(R.id.ItemTitle)
            if (textView?.text == tabText) {
                return i
            }
        }
        return -1 // 如果未找到，返回 -1
    }

    fun removeTabSpec(tabText: String) {
        val index = getTabIndex(tabText)
        if (index >= 0) {
            fragments.removeAt(index) // 移除对应的 Fragment
            views.removeAt(index) // 移除对应的视图
            tabLayout.removeTabAt(index) // 移除 Tab
            adapter.notifyDataSetChanged() // 通知适配器更新
        }
    }



    fun updateHighlight() {
        val currentTab = tabLayout.selectedTabPosition
        if (currentTab > -1) {
            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.customView?.alpha = if (i == currentTab) 1f else 0.3f
            }
        }
    }
    
    fun indexOfTab(tabText: String): Int {
    for (i in 0 until tabLayout.tabCount) {
        val tab = tabLayout.getTabAt(i)
        val textView = tab?.customView?.findViewById<TextView>(R.id.ItemTitle)
        if (textView?.text == tabText) {
            return i
        }
    }
    return -1 // 返回 -1 如果未找到
}

fun removeTabAt(index: Int) {
    if (index >= 0 && index < tabLayout.tabCount) {
        tabLayout.removeTabAt(index) // 从 TabLayout 中移除 Tab
        fragments.removeAt(index) // 从 Fragments 中移除对应的 Fragment
        views.removeAt(index) // 从视图列表中移除对应的视图
        adapter.notifyDataSetChanged() // 通知适配器更新
    }
}


    init {
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            private fun updateTab() {
                if (!tabsInited) {
                    for (i in 0 until tabLayout.tabCount) {
                        tabLayout.getTabAt(i)?.setCustomView(views[i])
                    }
                    tabsInited = true
                }
                updateHighlight()
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTab()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
