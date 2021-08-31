package com.example.kotlinmessenger.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityChatLogBinding
import com.example.kotlinmessenger.databinding.ChatFromRowBinding
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.example.kotlinmessenger.model.ChatMessage
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.facebook.drawee.backends.pipeline.Fresco
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

        // Fresco初期化
        Fresco.initialize(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_log)
        binding.userPageViewModel = viewModel
        binding.lifecycleOwner = this
        Log.d("log", "ChatLog start")

        toUser = intent.getParcelableExtra<User>(viewModel.USER_KEY)
        supportActionBar?.title = toUser?.userName

        viewModel.listenForMessages(toUser, this)
        recyclerview_chat_log.adapter = viewModel.ChatLogAdapter.value
        recyclerview_chat_log.setHasFixedSize(true)
        recyclerview_chat_log.setItemViewCacheSize(20)
//        recyclerview_chat_log.setDrawingCacheEnabled(true)
//        recyclerview_chat_log.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH)

        val imagePipeline = Fresco.getImagePipeline()
        // Clear cache
        imagePipeline.clearMemoryCaches()
        imagePipeline.clearDiskCaches()


        send_button_chat_log.setOnClickListener {
            Log.d("log", "attempt to send message...")
            viewModel.performSendMessage(this)
        }
        image_select_button.setOnClickListener {
            viewModel.imageSelecterStart(this)
        }

        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.imageSelectedFunction(data, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            // ChatLogActivityから離れる際にaddChildEventListenerをデタッチする
            android.R.id.home -> {
                Log.d("log", "push backbutton in actionbar")
                viewModel.eventlistenerFinish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("log", "end activity")
        // ChatLogActivityから離れる際にaddChildEventListenerをデタッチする
        viewModel.eventlistenerFinish()
    }


}


