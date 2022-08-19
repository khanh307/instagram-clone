package com.example.instagram.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Adapter.UserAdapter
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowingFragment : Fragment() {
    private var profileId: String? = ""
    private var recyclerView: RecyclerView? = null
    private var followingArray: ArrayList<User>? = null
    private var followingAdapter: UserAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_following, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_following_frag)
        followingArray = ArrayList()
        followingAdapter = activity?.let { UserAdapter(it, followingArray!!) }
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView!!.layoutManager = linearLayoutManager
        recyclerView!!.adapter = followingAdapter

        val pref = context?.getSharedPreferences("PREFSFL", Context.MODE_PRIVATE)
        if(pref != null){
            this.profileId = pref.getString("profile", "none").toString()
        }
        getFollowing()

        return view
    }

    private fun getFollowing(){
        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Following")
        followingArray!!.clear()
        followingsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    for(snapshot in datasnapshot.children){
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users").child(snapshot.key.toString())
                        userRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot1: DataSnapshot) {
                                if(snapshot1.exists()){
                                    var user = snapshot1.getValue<User>(User::class.java)
                                    if(user!!.getUid() != profileId){
                                            followingArray!!.add(user)
                                    }
                                }
                                followingAdapter!!.notifyDataSetChanged()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


}