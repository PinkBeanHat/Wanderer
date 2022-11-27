package com.wanderer.client

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wanderer.client.activitiy.FriendActivity

class FriendViewAdapter(activity: FriendActivity): FragmentStateAdapter(activity) {
    private val fragList = ArrayList<Fragment>()

    init {
        fragList.add(FriendReceiveFragment())
        fragList.add(FriendSendFragment())
    }

    override fun getItemCount(): Int = fragList.size

    override fun createFragment(position: Int): Fragment = fragList[position]

}