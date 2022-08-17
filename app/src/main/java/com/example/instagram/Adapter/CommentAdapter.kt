package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.Comment
import com.example.instagram.R
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class CommentAdapter(var mContext: Context, var mComment: List<Comment>): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var imageComment: ImageView
        var userName: TextView
        var commenttext: TextView
        var time: TextView
        init {
            imageComment = itemView.findViewById(R.id.comment_image)
            userName = itemView.findViewById(R.id.user_name_comment)
            commenttext = itemView.findViewById(R.id.context_comment)
            time = itemView.findViewById(R.id.time_comment)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var comm = mComment[position]
        Picasso.get().load(comm.image).placeholder(R.drawable.profile).into(holder.imageComment)
        holder.userName.text = comm.username
        holder.commenttext.text = comm.text
        holder.time.text = comm.time
    }

    override fun getItemCount(): Int {
        return mComment.size
    }


}