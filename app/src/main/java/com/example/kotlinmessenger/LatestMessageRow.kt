package com.example.kotlinmessenger

import android.util.Log
import com.example.kotlinmessenger.databinding.LatestMessageRowBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class LatestMessageRow(val chatMessage: ChatMessage): BindableItem<LatestMessageRowBinding>() {
    var chatPartnerUser: User? = null

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
                    chatPartnerUser = snapshot.getValue(User::class.java)!!
                    viewBinding.usernameTextviewLatestMessager.text = chatPartnerUser?.username

                    val targetImageView = viewBinding.imageviewLatestMessager
                    Picasso.get().load(chatPartnerUser?.profileImageUri).into(targetImageView)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}
