package com.example.instagram.Fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.FollowActivity
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if(pref != null){
            this.profileId = pref.getString("profileId", "none").toString()
        }
        if(profileId == firebaseUser.uid){
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if(profileId != firebaseUser.uid){
            checkFollowAndFollowingButtonStatus()
        }

        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()
            when {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" ->{

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Follower").child(it1.toString()).setValue(true)
                    }
                }

                getButtonText == "Following" ->{
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Follower").child(it1.toString()).removeValue()
                    }
                }
            }

        }



        view.layout_follower.setOnClickListener {
            val mIntent = Intent(context, FollowActivity::class.java)
            mIntent.putExtra("ID", profileId)
            mIntent.putExtra("selected", 1)
            mIntent.putExtra("userName", view.profile_fragment_username.text.toString())
            startActivity(mIntent)
        }
        view.layout_following.setOnClickListener {
            val mIntent = Intent(context, FollowActivity::class.java)
            mIntent.putExtra("ID", profileId)
            mIntent.putExtra("selected", 2)
            mIntent.putExtra("userName", view.profile_fragment_username.text.toString())
            startActivity(mIntent)
        }

        getFollowers()
        getFollowing()
        userInfo()

        return view
    }


    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if(followingRef != null){
            followingRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(datasnapshot: DataSnapshot) {
                    if(datasnapshot.child(profileId).exists()){
                        view?.edit_account_settings_btn?.text = "Following"
                        view?.edit_account_settings_btn?.setTextColor(Color.parseColor("#f8ffff"))
                        view?.edit_account_settings_btn?.setBackgroundResource(R.drawable.button_follow)
                    } else {
                        view?.edit_account_settings_btn?.text = "Follow"
                        view?.edit_account_settings_btn?.setTextColor(Color.parseColor("#040404"))
                        view?.edit_account_settings_btn?.setBackgroundResource(R.drawable.button_unfollow)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")


        followersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.total_followers?.text = (snapshot.childrenCount-1).toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowing(){
        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")


        followingsRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.total_following?.text = (snapshot.childrenCount-1).toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun userInfo(){
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    var user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .into(view?.pro_image_profile_frag)
                    view?.profile_fragment_username?.text = user?.getUsername()
                    view?.full_name_profile_frag?.text = user?.getFullname()
                    view?.bio_profile_frag?.text = user?.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }
}