package com.example.instagramcloneapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Model.Story
import com.example.instagramcloneapp.R
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter (private val mContext: Context,private val mStory: List<Story>) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var story_image_seen: CircleImageView? = null
        var story_image: CircleImageView? = null
        var story_username: TextView? = null

        var story_plus_btn: ImageView? = null
        var add_story_text: TextView? = null



        init {
            story_image_seen = itemView.findViewById(R.id.story_image_seen)
            story_image = itemView.findViewById(R.id.story_image)
            story_username = itemView.findViewById(R.id.story_user_name)

            story_plus_btn = itemView.findViewById(R.id.story_add)
            add_story_text = itemView.findViewById(R.id.add_story_text)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 0
        }
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]
    }

}