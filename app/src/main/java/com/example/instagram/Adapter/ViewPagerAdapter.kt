package com.example.instagram.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagram.Fragments.FollowerFragment
import com.example.instagram.Fragments.FollowingFragment
import com.example.instagram.Fragments.HomeFragment

class ViewPagerAdapter: FragmentStateAdapter {

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)


    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return FollowerFragment()
            1 -> return FollowingFragment()
        }
        return null as Fragment
    }
}