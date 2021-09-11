package com.example.kotlinmessenger.model

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.LatestMessageRowBinding
import com.example.kotlinmessenger.view.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

data class LatestMessageRow(val chatMessage: ChatMessage, val childChangeType: String): BindableItem<LatestMessageRowBinding>() {
    var chatPartnerUser: User? = null

    override fun bind(viewBinding: LatestMessageRowBinding, position: Int) {
        viewBinding.messageTextviewLatestMessager.text = if(chatMessage.text != "") chatMessage.text else "画像を送信しました"

        // 自分以外(toId or fromId)のユーザーIDを取得
        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else {
            chatMessage.fromId
        }

//        Log.d("listenForLatestMessages", "chatPartnerId: $chatPartnerId")

        // chatPartnerIdからユーザー情報取得・表示
        FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
            .addListenerForSingleValueEvent( object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d("listenForLatestMessages", "users")
                    chatPartnerUser = snapshot.getValue(User::class.java)!!
                    viewBinding.usernameTextviewLatestMessager.text = chatPartnerUser?.userName

                    val targetImageView = viewBinding.imageviewLatestMessager
                    Picasso.get().load(chatPartnerUser?.profileImageUri).into(targetImageView)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        // メッセージが追加された際にbackgroundColorアニメーション
        if (childChangeType == "Change" && chatMessage.fromId != FirebaseAuth.getInstance().uid) {

            ValueAnimator().apply {
                    setIntValues(Color.argb(50,97, 189, 255), Color.argb(0,255,255,255))
                    setEvaluator(ArgbEvaluator())
                    addUpdateListener {
                        viewBinding.latestMessageRowCardview.setBackgroundColor(it.animatedValue as Int)
                    }
                    duration = 2000
                    start()
            }
        }

        // 未読の場合ベルアニメーション表示
        if (!chatMessage.alreadyRead) {
            viewBinding.newMessageIAnimIcon.visibility = View.VISIBLE
        }

    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}
