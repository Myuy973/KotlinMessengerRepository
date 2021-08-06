package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinmessenger.model.ChatMessage
import com.example.kotlinmessenger.model.LatestMessageRow
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.view.LatestMessagesActivity
import com.example.kotlinmessenger.view.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupieAdapter

class UserPageViewModel: ViewModel() {

    var adapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    private val latestMessageMap = HashMap<String, ChatMessage>()

    fun verifyUserIsLoggedIn(activity: Activity) {
        // firebaseにログインしているかどうか
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(activity, RegisterActivity::class.java)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

     fun fetchCurrentUser() {
        val uid = Firebase.auth.currentUser?.uid
        FirebaseDatabase.getInstance().getReference("/users/$uid")
                .addListenerForSingleValueEvent( object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            LatestMessagesActivity.currentUser = snapshot.getValue(User::class.java)!!
                            Log.d("value", "Login user: ${LatestMessagesActivity.currentUser.username}")
                        } catch (e: Exception) {
                            Log.d("value", "login user error: $e")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.value?.clear()
        latestMessageMap.values.forEach {
            adapter.value?.add(LatestMessageRow(it))
        }
    }

    fun listenForLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                Log.d("listenForLatestMessages", "database add changed!!! : ${chatMessage.text}")
                Log.d("listenForLatestMessages", "snapshot changed!!! : ${snapshot}")

                latestMessageMap[snapshot.key!!] = chatMessage

                Log.d("listenForLatestMessages", "latestMessageMap: ${latestMessageMap[snapshot.key]}, id: ${snapshot.key}")

                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                Log.d("listenForLatestMessages", "database onChildChanged!!!: ${chatMessage.text}")

                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }



}