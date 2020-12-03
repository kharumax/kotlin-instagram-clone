package com.example.instagramcloneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Adapter.CommentsAdapter
import com.example.instagramcloneapp.Model.Comment
import com.example.instagramcloneapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager =  LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this,commentList)
        recyclerView.adapter = commentAdapter

        userInfo()
        readComments()
        getPostImage()

        post_comments.setOnClickListener {
            if (add_comments.text.toString() == "") {
                Toast.makeText(this,"Please write comments",Toast.LENGTH_LONG).show()
            } else {
                addComment()
            }
        }

    }

    private fun readComments() {
        val commentRef = FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)
        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    commentList!!.clear()
                    for (sp in snapshot.children) {
                        val comment = sp.getValue(Comment::class.java)
                        Log.d("readComments","readComments is called and comment is $comment")
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        val commentMap = HashMap<String,Any>()
        commentMap["comment"] = add_comments.text.toString()
        commentMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentMap)

        addNotification()

        add_comments.text.clear()
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comments)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val image = snapshot.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comment)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addNotification() {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(publisherId)
        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "commented: " + add_comments.text.toString()
        notiMap["postid"] = postId
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}