package com.example.instagram

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instagram.Adapter.CommentAdapter
import com.example.instagram.Model.Comment
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comments.*
import java.text.SimpleDateFormat
import java.util.*

class CommentsActivity : AppCompatActivity() {
    private var postId: String = ""
    private var mComment: ArrayList<Comment>? = null
    private var commentAdapter: CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        if(intent.extras != null){
            postId = intent.getStringExtra("postid").toString()
        }



        mComment = ArrayList()
        commentAdapter = CommentAdapter(this, mComment!!)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recycler_view_comments.layoutManager = linearLayoutManager
        recycler_view_comments.adapter = commentAdapter
        loadComments()
        getPublisher()
        getUser()

        post_comment_btn.setOnClickListener {
            postComment()
        }
        back_btn.setOnClickListener {
            finish()
        }
    }

    private fun getPublisher() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts").child(postId)
        postRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val post = snapshot.getValue(Post::class.java)
                    val userRef = FirebaseDatabase.getInstance().reference
                        .child("Users").child(post!!.getPublisher())
                    userRef.addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot2: DataSnapshot) {
                            val user = snapshot2.getValue(User::class.java)
                            Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(publisher_image)
                            publisher_name.text = user?.getUsername()
                            description_comments.text = post.getDescription()
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getUser(){
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid.toString())
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user?.getImage()).placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(user_image)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun postComment() {
        val c: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MM-yyy HH:mm")
        val strDate = sdf.format(c.time)

        Log.d("GGG", strDate.toString())
        val commRef = FirebaseDatabase.getInstance().reference
            .child("LikesAndComments").child(postId).child("Comments")
            .child(FirebaseAuth.getInstance().uid.toString()).child(strDate.toString())
            .setValue(add_comment_edit_text.text.toString()).addOnCompleteListener { task->
                if(task.isSuccessful){
                    add_comment_edit_text.setText("")
                    Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                    loadComments()
                }
            }

    }

    private fun loadComments(){
        var commRef = FirebaseDatabase.getInstance().reference
            .child("LikesAndComments").child(postId).child("Comments")

        commRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if(datasnapshot.exists()){
                    mComment?.clear()
                    for(snapshot in datasnapshot.children){
                        if(snapshot.exists()){
                            for(sn in snapshot.children){
                                var uid = snapshot.key.toString()
                                var text = sn.value.toString()
                                var time = sn.key.toString()
                                mComment?.add(Comment(uid, "", "R.drawable.profile", text, time))
                            }
                        }
                    }

                    for(i in 0 until mComment!!.size){
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users").child(mComment!![i].uid.toString())

                        userRef.addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()){
                                    val user = snapshot.getValue<User>(User::class.java)
                                    if(user != null){
                                        mComment!![i].username = user.getUsername()
                                        mComment!![i].image = user.getImage()
                                        Log.d("AAA", user.getUsername())
                                    }
                                }
                                commentAdapter?.notifyDataSetChanged()
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