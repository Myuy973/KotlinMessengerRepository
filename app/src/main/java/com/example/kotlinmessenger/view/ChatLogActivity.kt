package com.example.kotlinmessenger.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityChatLogBinding
import com.example.kotlinmessenger.databinding.ChatFromRowBinding
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.example.kotlinmessenger.model.ChatMessage
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.databinding.BindableItem
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private val viewModel: UserPageViewModel by viewModels()

    private lateinit var binding: ActivityChatLogBinding
    private var toUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_log)
        binding.userPageViewModel = viewModel
        binding.lifecycleOwner = this

        toUser = intent.getParcelableExtra<User>(viewModel.USER_KEY)
        supportActionBar?.title = toUser?.userName

        viewModel.listenForMessages(toUser, this)
        recyclerview_chat_log.adapter = viewModel.ChatLogAdapter.value

        send_button_chat_log.setOnClickListener {
            Log.d("value", "attempt to send message...")
            viewModel.performSendMessage(this)
        }
        image_select_button.setOnClickListener {
            viewModel.imageSelecterStart(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.imageSelectedFunction(data, this)
    }

}


