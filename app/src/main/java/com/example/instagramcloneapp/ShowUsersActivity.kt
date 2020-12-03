package com.example.instagramcloneapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Adapter.UserAdapter
import com.example.instagramcloneapp.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*

class ShowUsersActivity : AppCompatActivity() {

    var id: String = ""
    var title: String = ""

    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this,userList as ArrayList<User>,false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()
        when(title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    for (id in idList!!) {
                        if (user.getUID() == id) {
                            (userList as ArrayList<User>).add(user)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes").child(id)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idList as ArrayList<String>).clear()
                    for (sp in snapshot.children) {
                        (idList as ArrayList<String>).add(sp.key!!)
                    }
                    showUsers()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getFollowing() {
        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in dataSnapshot.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in dataSnapshot.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun getViews() {}
}