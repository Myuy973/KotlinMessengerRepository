package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityNewMessageBinding
import com.example.kotlinmessenger.databinding.UserRowNewMessageBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.databinding.BindableItem
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var binding: ActivityNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_message)

        recyclerview_newmessage.adapter = viewModel.NewMessageAdapter.value

        supportActionBar?.title = "Select User"

        viewModel.fetchUsers(this)

    }
}