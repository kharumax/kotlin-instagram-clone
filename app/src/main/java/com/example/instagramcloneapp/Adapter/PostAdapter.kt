package com.example.instagramcloneapp.Adapter

import android.content.Context
import android.content.Intent
import android.renderscript.Sampler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.CommentsActivity
import com.example.instagramcloneapp.MainActivity
import com.example.instagramcloneapp.Model.Post
import com.example.instagramcloneapp.Model.User
import com.example.instagramcloneapp.R
import com.example.instagramcloneapp.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*

class PostAdapter(private val mContext: Context,private val mPost: List<Post>) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.getPostimage()).into(holder.postImage)

        if (post.getDescription().equals("")) {
            holder.description.visibility == View.GONE
        } else {
            holder.description.visibility == View.VISIBLE
            holder.description.text = post.getDescription()
        }

        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.getPublisher())
        isLikes(post.getPostid(),holder.likeButton)
        numberOfLikes(post.getPostid(),holder.likes)
        getTotalComments(post.getPostid(),holder.comments)
        checkSavedStatus(post.getPostid(),holder.saveButton)

        holder.likeButton.setOnClickListener {
            if (it.tag == "Like" ) {
                // いいねする
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid()).child(firebaseUser!!.uid).setValue(true)
                addNotification(post.getPublisher(),post.getPostid())
            } else {
                // いいねを外す
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid()).child(firebaseUser!!.uid).removeValue()
            }
        }

        holder.commentButton.setOnClickListener {
            val intent = Intent(mContext,CommentsActivity::class.java)
            intent.putExtra("postId",post.getPostid())
            intent.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext,CommentsActivity::class.java)
            intent.putExtra("postId",post.getPostid())
            intent.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intent)
        }
        holder.saveButton.setOnClickListener {
            Log.d("PostAdapter","Clicked and ${holder.saveButton.tag}")
            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).removeValue()
            }
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostid())
            intent.putExtra("title","likes")
            mContext.startActivity(intent)
        }
    }

    private fun getTotalComments(postid: String, comments: TextView) {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    comments.visibility = View.VISIBLE
                    comments.text = "view all " + snapshot.childrenCount.toString() + " comments"
                } else {
                    comments.visibility == View.GONE
                    comments.text = ""
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun numberOfLikes(postid: String, likes: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PostAdapter","onDataChange in numberOfLikes is called!!")
                if (snapshot.exists()) {
                    likes.visibility = View.VISIBLE
                    likes.text = snapshot.childrenCount.toString() + " likes"
                } else {
                    likes.visibility == View.GONE
                    likes.text = ""
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PostAdapter","onDataChange in isLikes is called!!")
                if (snapshot.child(firebaseUser!!.uid).exists()) {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
        usersRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user.getUsername()
                    publisher.text = user.getFullname()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkSavedStatus(postid: String,imageView: ImageView)  {
        val savesRef = FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
        savesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addNotification(userId: String,postId: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "like your post"
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }


}