package com.example.instagram

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.UserAdapter
import com.example.instagram.Adapter.ViewPagerAdapter
import com.example.instagram.Fragments.HomeFragment
import com.example.instagram.Fragments.NotificationsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.Fragments.SearchFragment
import com.example.instagram.Model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_follow.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class FollowActivity : AppCompatActivity() {
    private var profileId: String? = ""
    private var followingArray: ArrayList<User>? = null
    private var followerArray: ArrayList<User>? = null
    private var followerAdapter: UserAdapter? = null
    private var followingAdapter: UserAdapter? = null

    private var viewPagerAdapter: ViewPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        viewPagerAdapter = ViewPagerAdapter(this)
        view_pager.adapter = viewPagerAdapter
        TabLayoutMediator(tab_layout, view_pager, object:
            TabLayoutMediator.TabConfigurationStrategy {
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                when(position){
                    0 -> tab.setText("Follower")
                    1 -> tab.setText("Following")
                }
            }

        }).attach()

        var selected = 1
        val intent = intent
        if(intent.extras != null){
            profileId = intent.getStringExtra("ID")
            follow_fragment_username.text = intent.getStringExtra("userName")
            selected = intent.getIntExtra("selected", 1)
        }

        val pref = this.getSharedPreferences("PREFSFL", Context.MODE_PRIVATE).edit()
        pref.putString("profile", profileId)
        pref.apply()

        if(selected == 2){
           var tab: TabLayout.Tab? = tab_layout.getTabAt(1)
            tab?.select()
        }

        back_btn.setOnClickListener {
            finish()
        }

    }




}