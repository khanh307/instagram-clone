package com.example.instagram

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FollowAdapter(private var mContext: Context, private var mUser: List<User>): RecyclerView.Adapter<FollowAdapter.ViewHolder>() {

    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user = mUser[position]
        holder.userFullnameTextView.text = user.getFullname()
        holder.userNameTextView.text = user.getUsername()
        Picasso.get().load(user.getImage())
            .placeholder(R.drawable.profile)
            .into(holder.userProfileImage)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

}