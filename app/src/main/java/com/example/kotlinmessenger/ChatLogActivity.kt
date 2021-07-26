package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.databinding.ActivityChatLogBinding
import com.example.kotlinmessenger.databinding.ChatFromRowBinding
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.databinding.BindableItem
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatLogBinding
    private val adapter = GroupieAdapter()
    private var toUser: User? = null

    companion object {
        val TAG = "ChatLog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_log)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
//        val username = intent.getStringExtra(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username

//        setupDummyData()
        listenForMessages()
        recyclerview_chat_log.adapter = adapter
        recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

        send_button_chat_log.setOnClickListener {
            Log.d("value", "attempt to send message...")
            performSendMessage()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = toUser?.uid!!
        val ref = FirebaseDatabase.getInstance()
            .getReference("user-messages/$fromId/$toId")

        ref.addChildEventListener( object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("value", "add start")
                Log.d("value", "adapter: ${adapter.itemCount}")
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d("value", chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun performSendMessage() {

        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = toUser?.uid!!

        val reference = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId")
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId")

        // クラスにして送らないと送信できない(Activityも落ちる)
        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("value", "Saved our chat message to From: ${it}")
                Log.d("value", "adapter.itemcount: ${adapter.itemCount}")
                Log.d("value", "adapter.itemcount - 1: ${adapter.itemCount - 1}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("value", "Saved our chat message to To: ${it}")
            }


        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }


}


class ChatFromItem(val text: String, val user: User): BindableItem<ChatFromRowBinding>() {
    override fun bind(viewBinding: ChatFromRowBinding, position: Int) {
        viewBinding.textView.text = text

        val uri = user.profileImageUri
        val targetimageView = viewBinding.imageviewChatFromRow
        Picasso.get().load(uri).into(targetimageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
class ChatToItem(val text: String, val user: User): BindableItem<ChatToRowBinding>() {
    override fun bind(viewBinding: ChatToRowBinding, position: Int) {
        viewBinding.textView.text = text

        val uri = user.profileImageUri
        val targetimageView = viewBinding.imageviewChatToRow
        Picasso.get().load(uri).into(targetimageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}