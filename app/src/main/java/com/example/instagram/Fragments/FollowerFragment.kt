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


class FollowerFragment : Fragment() {
    private var profileId: String? = ""
    private var recyclerView: RecyclerView? = null
    private var followerArray: ArrayList<User>? = null
    private var followerAdapter: UserAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_follower, container, false)
        recyclerView = view.findViewById(R.id.recycler_view_follower_frag)
        followerArray = ArrayList()
        followerAdapter = activity?.let { UserAdapter(it, followerArray!!) }
        val linearLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = followerAdapter

        val pref = context?.getSharedPreferences("PREFSFL", Context.MODE_PRIVATE)
        if(pref != null){
            this.profileId = pref.getString("profile", "none").toString()
        }
        getFollowers()
        Log.d("DDD", profileId!!)
        return view
    }

        private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Followers")
        followerArray!!.clear()
        followersRef.addValueEventListener(object: ValueEventListener {
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
                                        followerArray!!.add(user)
                                    }
                                }
                                followerAdapter!!.notifyDataSetChanged()
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