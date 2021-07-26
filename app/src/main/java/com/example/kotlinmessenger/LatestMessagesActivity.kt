package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.databinding.ActivityLatestMessagesBinding
import com.example.kotlinmessenger.databinding.LatestMessageRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.databinding.BindableItem
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLatestMessagesBinding
    private var adapter = GroupieAdapter()
    private val latestMessageMap = HashMap<String, ChatMessage>()


    companion object {
        lateinit var currentUser: User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_latest_messages)

        recycler_latest_messages.adapter = adapter
        // カード間にボーダー
        recycler_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()
    }

    class LatestMessageRow(val chatMessage: ChatMessage): BindableItem<LatestMessageRowBinding>() {
        override fun bind(viewBinding: LatestMessageRowBinding, position: Int) {
            viewBinding.messageTextviewLatestMessager.text = chatMessage.text

            val chatPartnerId: String
            Log.d("listenForLatestMessages", "text: ${chatMessage.text}")
            Log.d("listenForLatestMessages", "fromid: ${chatMessage.fromId}")
            Log.d("listenForLatestMessages", "loginuser: ${FirebaseAuth.getInstance().uid}")
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            } else {
                chatPartnerId = chatMessage.fromId
            }

            Log.d("listenForLatestMessages", "chatPartnerId: $chatPartnerId")

            FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
                .addListenerForSingleValueEvent( object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("listenForLatestMessages", "users")
                        val userObject = snapshot.getValue(User::class.java)!!
                        viewBinding.usernameTextviewLatestMessager.text = userObject.username

                        val targetImageView = viewBinding.imageviewLatestMessager
                        Picasso.get().load(userObject.profileImageUri).into(targetImageView)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private  fun listenForLatestMessages() {
        try {
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
        } catch (e: Exception) {
            Log.d("listenForLatestMessages", "error: $e")
        }
    }


    private fun fetchCurrentUser() {
        val uid = Firebase.auth.currentUser?.uid
        FirebaseDatabase.getInstance().getReference("/users/$uid")
            .addListenerForSingleValueEvent( object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        currentUser  = snapshot.getValue(User::class.java)!!
                        Log.d("value", "Login user: ${currentUser.username}")
                    } catch (e: Exception) {
                        Log.d("value", "login user error: $e")
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun verifyUserIsLoggedIn() {
        // firebaseにログインしているかどうか
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}