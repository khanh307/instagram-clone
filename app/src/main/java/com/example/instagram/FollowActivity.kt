package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.UserAdapter
import com.example.instagram.Fragments.HomeFragment
import com.example.instagram.Fragments.NotificationsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.Fragments.SearchFragment
import com.example.instagram.Model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)
        followerArray = ArrayList()
        followingArray = ArrayList()
        followerAdapter = UserAdapter(this, followerArray!!)
        followingAdapter = UserAdapter(this, followingArray!!)
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_view_follow.layoutManager = linearLayoutManager
        recycler_view_follow.adapter = followerAdapter
        var selected = 1
        val intent = intent
        if(intent.extras != null){
            profileId = intent.getStringExtra("ID")
            follow_fragment_username.text = intent.getStringExtra("userName")
            selected = intent.getIntExtra("selected", 1)
        }

        back_btn.setOnClickListener {
            finish()
        }

        getFollowers()
        getFollowing()
        nav_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        if(selected == 2){
            nav_view.selectedItemId = R.id.following
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.follower -> {
                Log.d("EEE", "1")
                recycler_view_follow.adapter = followerAdapter
                recycler_view_follow.adapter!!.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.following -> {
                Log.d("EEE", "2")
                recycler_view_follow.adapter = followingAdapter
                recycler_view_follow.adapter!!.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Followers")
        followerArray!!.clear()
        followersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(snapshot in datasnapshot.children){
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users").child(snapshot.key.toString())
                        userRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot1: DataSnapshot) {
                                if(snapshot1.exists()){
                                    var user = snapshot1.getValue<User>(User::class.java)
                                    Log.d("EEE", user!!.getUid())
                                    if(user!!.getUid() != profileId){
                                        followerArray!!.add(user)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                    followerAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowing(){
        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Following")
        followingArray!!.clear()

        followingsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(snapshot in datasnapshot.children){
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users").child(snapshot.key.toString())
                        userRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot1: DataSnapshot) {
                                if(snapshot1.exists()){
                                    var user = snapshot1.getValue<User>(User::class.java)
                                    Log.d("EEE", user!!.getUid())
                                    if(user!!.getUid() != profileId){
                                            followingArray!!.add(user)
                                    }

                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                    followingAdapter!!.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}