package com.example.instagramcloneapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Fragments.ProfileFragment
import com.example.instagramcloneapp.Model.User
import com.example.instagramcloneapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.user_item_layout.view.*

class UserAdapter (private var mContext: Context, private var mUser: List<User>, private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout,parent,false)
        return  UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mUser.size
    }

    //　ここの関数マジで汚い。フォローとアンフォローでモジュール化できる。
    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTextView.text = user.getUsername()
        holder.userFullnameTextView.text = user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)

        checkFollowingStatus(user.getUID(),holder.followButton)

        holder.itemView.setOnClickListener {
            // メモリ内に環境変数の読み書きを行う.(PREFSという名称で)
            val pref = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            pref.putString("profileId",user.getUID())
            pref.apply()

            // ここでitemViewがタップされた時の遷移を行っている
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,ProfileFragment()).commit()
        }

        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow") {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // ここには特に処理が必要ない
                                                // UIの更新は他の関数で監視・更新している
                                            }
                                        }
                                }
                            }
                        }
                }
                addNotification(user.getUID())
            } else {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUID())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUID())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // ここには特に処理が必要ない
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    // viewに表示する画面部品の初期化
    class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    // ここでフォローの状態を監視して、UIの更新も行っている
    private fun checkFollowingStatus(uid: String,followButton: Button) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        // EventListenerを追加して値が更新された時の処理をセットしておく
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid).exists()) {
                    // DBにデータが存在していた場合
                    followButton.text = "Following"
                } else {
                    followButton.text = "Follow"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    private fun addNotification(userId: String) {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)
        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you"
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }
}