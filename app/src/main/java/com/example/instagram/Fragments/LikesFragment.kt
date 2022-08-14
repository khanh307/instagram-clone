package com.example.instagram.Fragments

import android.annotation.SuppressLint
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LikesFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private lateinit var postId: String
    private lateinit var firebaseUser: FirebaseUser
    private var userAdapter: UserAdapter? = null
    private var mUser: ArrayList<User>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_likes, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_like)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        mUser = ArrayList()
        userAdapter = context?.let{ UserAdapter(it, mUser!!, true) }

        recyclerView?.adapter = userAdapter

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFPOST", Context.MODE_PRIVATE)
        if(pref != null){
            this.postId = pref.getString("postid", "none").toString()
        }

        getUser()

        return view
    }

    private fun getUsers(){
        val userRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(s in snapshot.children){
                        var user = s.getValue<User>(User::class.java)
                        if(user != null){
                            mUser?.add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getUser() {

        val likeRef = FirebaseDatabase.getInstance().getReference()
            .child("LikesAndComments").child(postId)
            .child("Likes")
        likeRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    mUser!!.clear()
                    for(snapshot in datasnapshot.children){
                        var userRef = FirebaseDatabase.getInstance().reference
                            .child("Users").child(snapshot.key.toString())
                        userRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot1: DataSnapshot) {
                                if(snapshot1.exists()){
                                    var user = snapshot1.getValue<User>(User::class.java)
                                    if(user != null){
                                        mUser!!.add(user)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                    }
                    userAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


}