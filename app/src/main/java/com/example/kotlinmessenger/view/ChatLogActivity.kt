package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityChatLogBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
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
//        Log.d("log", "ChatLog start")

        // チャット相手の取得、タイトルにセット
        toUser = intent.getParcelableExtra<User>(viewModel.USER_KEY)
        supportActionBar?.title = toUser?.userName

        // チャットデータ収集
        viewModel.listenForMessages(toUser, this)
        recyclerview_chat_log.adapter = viewModel.ChatLogAdapter.value
        recyclerview_chat_log.setHasFixedSize(true)
        recyclerview_chat_log.setItemViewCacheSize(20)


        send_button_chat_log.setOnClickListener {
//            Log.d("log", "attempt to send message...")
            viewModel.performSendMessage(this)
        }
        image_select_button.setOnClickListener {
            viewModel.imageSelecterStart(this)
        }

        // 画面遷移アニメーション
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
//                Log.d("log", "push backbutton in actionbar")
                viewModel.eventlistenerFinish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // ChatLogActivityから離れる際にaddChildEventListenerをデタッチする
        viewModel.eventlistenerFinish()
    }


}


