package com.wency.petmanager.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class CreatePageAdapter(childrenFragmentManager: FragmentManager
                        , lifecycle: Lifecycle
                        , private val fragmentList: ArrayList<Fragment>
                        ) : FragmentStateAdapter(childrenFragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}