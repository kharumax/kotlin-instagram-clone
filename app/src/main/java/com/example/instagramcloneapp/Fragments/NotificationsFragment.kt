package com.example.instagramcloneapp.Fragments

import com.example.instagramcloneapp.Model.Notification
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramcloneapp.Adapter.NotificationAdapter
import com.example.instagramcloneapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class NotificationsFragment : Fragment() {

    private var notificationList: List<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        var recyclerView: RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_notifications)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()
        notificationAdapter = context?.let { NotificationAdapter(it, notificationList as ArrayList<Notification>) }
        recyclerView.adapter = notificationAdapter

        readNotifications()

        return view
    }

    private fun readNotifications() {
        val notiRef = FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        notiRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (notificationList as ArrayList<Notification>).clear()
                    for (sp in snapshot.children) {
                        val notification = sp.getValue(Notification::class.java)!!
                        (notificationList as ArrayList<Notification>).add(notification)
                    }
                    Collections.reverse(notificationList)
                    notificationAdapter!!.notifyDataSetChanged()
                    Log.d("Notification","notificationList is $notificationList")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}