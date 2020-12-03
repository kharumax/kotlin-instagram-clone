package com.example.instagramcloneapp.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.AccountSettingsActivity
import com.example.instagramcloneapp.Adapter.MyImagesAdapter
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
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    var myImagesAdapterSaved: MyImagesAdapter? = null
    var postListSaved: List<Post>? = null
    var mySavesImg: List<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("DEBUG","onCreateView() is called")
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId","none").toString()
            Log.d("ProfileFragment","This is profileId $profileId")
        }
        // 現在のユーザーとプロファイルユーザーが同じか確認する
        if (profileId == firebaseUser.uid) {
            // 同じ場合の処理(
            view.edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }

        // recycler view for uploaded images
        var recyclerViewUploadImages: RecyclerView
        recyclerViewUploadImages = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImages.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewUploadImages.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it,postList as ArrayList<Post>)}
        recyclerViewUploadImages.adapter = myImagesAdapter

        // recycler view for saved images
        var recyclerViewSavedImages: RecyclerView
        recyclerViewSavedImages = view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewSavedImages.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSaved = context?.let { MyImagesAdapter(it,postListSaved as ArrayList<Post>)}
        recyclerViewSavedImages.adapter = myImagesAdapterSaved

        var uploadImageBtn: ImageButton
        uploadImageBtn = view.findViewById(R.id.images_grid_view_btn)
        uploadImageBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadImages.visibility = View.VISIBLE
        }

        var savedImageBtn: ImageButton
        savedImageBtn = view.findViewById(R.id.images_save_btn)
        savedImageBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.VISIBLE
            recyclerViewUploadImages.visibility = View.GONE
        }


        view.edit_account_settings_btn.setOnClickListener {
            val getButtonText = view.edit_account_settings_btn.text.toString()
            when {
                getButtonText == "Edit Profile" -> {
                    startActivity(Intent(context,AccountSettingsActivity::class.java))
                }
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).setValue(true)
                    }
                    addNotification()
                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId).removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString()).removeValue()
                    }
                }
            }
        }

        view.total_followers.setOnClickListener {
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","followers")
            startActivity(intent)
        }
        view.total_following.setOnClickListener {
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","following")
            startActivity(intent)
        }

        // ここでフォロー数とフォロワー数を取得する
        getFollowers()
        getFollowings()
        getTotalNumberOfPosts()
        userInfo()
        myPhotos()
        mySaves()

        return view
    }

    // フォローの状態を確認する
    private fun checkFollowAndFollowingButtonStatus() {
        val followRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        if (followRef != null) {
            followRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(profileId).exists()) {
                        // データが存在していた場合=フォローしていた場合
                        view?.edit_account_settings_btn?.text = "Following"
                    } else {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    // フォロワー数を取得する
    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // TODO(バグあり：１フォローからフォローを外す場合は、下のif文を満たさないのでテキストの値が更新されない)
                if (dataSnapshot.exists()) {
                    view?.total_followers?.text = dataSnapshot.childrenCount.toString()
                } else {
                    view?.total_followers?.text = "0"
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // フォロー数を取得する
    private fun getFollowings() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    view?.total_following?.text = dataSnapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun myPhotos() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (postList as ArrayList<Post>).clear()
                    for (sp in snapshot.children) {
                        val post = sp.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId) {
                            (postList as ArrayList<Post>).add(post)
                        }
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mySaves() {
        mySavesImg = ArrayList()
        var saveRef = FirebaseDatabase.getInstance().reference.child("Saves").child(profileId)
        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (sp in snapshot.children) {
                        (mySavesImg as ArrayList<String>).add(sp.key!!)
                    }
                    readSavedImageData()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun readSavedImageData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (postListSaved as ArrayList<Post>).clear()
                    for (sp in snapshot.children) {
                        val post = sp.getValue(Post::class.java)!!
                        for (id in mySavesImg!!) {
                            if (post.getPostid() == id) {
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSaved!!.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ユーザー情報を取得する
    private fun userInfo() {
        Log.d("DEBUG","userInfo() is called!")
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Log.d("DEBUG","This is user $user")
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_fragment)
                    view?.profile_fragment_username?.text = user!!.getUsername()
                    view?.full_name_profile_frag?.text = user!!.getFullname()
                    view?.bio_profile_frag?.text = user!!.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getTotalNumberOfPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var postCounter = 0
                    for (sp in snapshot.children) {
                        val post = sp.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId) {
                            postCounter++
                        }
                    }
                    total_posts.text = " " + postCounter
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun addNotification() {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(profileId)
        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

    // ライフサイクル？？？
    override fun onStop() {
        Log.d("DEBUG","onStop() is called")
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        Log.d("DEBUG","onPause() is called")
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        Log.d("DEBUG","onDestroy() is called")
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS",Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",firebaseUser.uid)
        pref?.apply()
    }

}