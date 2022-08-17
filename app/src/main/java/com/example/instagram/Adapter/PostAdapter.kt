package com.example.instagram.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.instagram.CommentsActivity
import com.example.instagram.Fragments.LikesFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.Listener.DoubleClickListener

import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context, private val mPost: List<Post>): RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null
    private var isLiked = false

    private lateinit var avd: AnimatedVectorDrawableCompat
    private lateinit var avd2: AnimatedVectorDrawable

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView
        var likesLayout:LinearLayout
        var heart: ImageView
        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
            likesLayout = itemView.findViewById(R.id.likes_layout)
            heart = itemView.findViewById(R.id.heart_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())
        holder.description.setText(post.getDescription())


        getLikes(post.getPostid(), holder.likes, holder.likeButton)
        holder.likeButton.setOnClickListener {
            likePost(post.getPostid(), holder.likeButton)
        }
        var drawable: Drawable = holder.heart.drawable


        holder.postImage.setOnClickListener (
            DoubleClickListener(
                callback = object : DoubleClickListener.Callback {
                    override fun doubleClicked() {
                        likePostNotRemove(post.getPostid(), holder.likeButton)
                        holder.heart.alpha = 0.7f
                        if(drawable is AnimatedVectorDrawableCompat){
                            avd = drawable as AnimatedVectorDrawableCompat
                            avd.start()
                        } else if(drawable is AnimatedVectorDrawable){
                            avd2 = drawable as AnimatedVectorDrawable
                            avd2.start()
                        }

                    }
                }
            )
        )

        holder.likesLayout.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFPOST", Context.MODE_PRIVATE).edit()
            pref.putString("postid", post.getPostid())
            pref.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LikesFragment()).commit()
        }

        holder.commentButton.setOnClickListener {
            val mIntent = Intent(mContext, CommentsActivity::class.java)
            mIntent.putExtra("postid", post.getPostid())
            mContext.startActivity(mIntent)
        }
    }

    private fun likePostNotRemove(postid: String, likeButton: ImageView) {
        if(isLiked == false){
            val likeRef = FirebaseDatabase.getInstance().reference
                .child("LikesAndComments").child(postid)
                .child("Likes")
                .child(firebaseUser!!.uid.toString()).setValue(true)
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        isLiked = true
                        likeButton.setImageResource(R.drawable.heart_clicked)
                    }
                }
        } else {
            return
        }
    }


    private fun likePost(postid: String, likeButton: ImageView) {
        if(isLiked == false){
            val likeRef = FirebaseDatabase.getInstance().reference
                .child("LikesAndComments").child(postid)
                .child("Likes")
                .child(firebaseUser!!.uid.toString()).setValue(true)
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        isLiked = true
                        likeButton.setImageResource(R.drawable.heart_clicked)
                    }
                }
        } else {
            val likeRef = FirebaseDatabase.getInstance().reference
                .child("LikesAndComments").child(postid)
                .child("Likes")
                .child(firebaseUser!!.uid.toString()).removeValue()
                .addOnCompleteListener{ task->
                    if(task.isSuccessful){
                        isLiked = false
                        likeButton.setImageResource(R.drawable.heart_not_clicked)
                    }
                }
        }
    }

    private fun getLikes(postid: String, likes: TextView, likeButton: ImageView){
        var likeRef = FirebaseDatabase.getInstance().reference
            .child("LikesAndComments").child(postid)
            .child("Likes")
        likeRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                likes.text = datasnapshot.childrenCount.toString()
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

        likeRef.child(firebaseUser!!.uid.toString()).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    isLiked = true
                    likeButton.setImageResource(R.drawable.heart_clicked)
                } else {
                    isLiked = false
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

}