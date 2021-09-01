package com.example.kotlinmessenger.view

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityLatestMessagesBinding
import com.example.kotlinmessenger.model.ChatMessage
import com.example.kotlinmessenger.model.LatestMessageRow
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupieAdapter
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.util.Calendar.getInstance

class LatestMessagesActivity : AppCompatActivity() {

    private val viewModel: UserPageViewModel by viewModels()

    private lateinit var binding: ActivityLatestMessagesBinding
    private lateinit var latestMessageResource: Resources


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_latest_messages)
        latestMessageResource = resources

        recycler_latest_messages.adapter = viewModel.LatestMessagesAdapter.value
        // カード間にボーダー
        recycler_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        viewModel.LatestMessagesAdapter.value?.setOnItemClickListener { item, view ->
            view.newMessageIAnimIcon.visibility = View.INVISIBLE
            val row = item as LatestMessageRow
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra(viewModel.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        val fromActivity = intent.getStringExtra("fromActivity")

        Log.d("log", "fromActivity: $fromActivity")

        when(fromActivity) {
            "SigninOrLogin" -> {
                Log.d("log", "fromActivity: SigninOrLogin")
                overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left)
            }
            else -> {
                Log.d("log", "fromActivity: else")
                overridePendingTransition(R.anim.anim_chat_ather_open, R.anim.anim_chat_main_close)
            }
        }

        viewModel.verifyUserIsLoggedIn(this)
        viewModel.fetchCurrentUser()
        viewModel.listenForLatestMessages()



    }



    // アクションバーでアイテムが押されたときのアクション
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.user_profile -> {
                val intent = Intent(this, ShowProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                AuthUI.getInstance().signOut(this).addOnSuccessListener {
                    val intent = Intent(this, RegisterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }.addOnFailureListener {
                    Log.d("log", "error: ${it.printStackTrace()}")
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // アクションバーのデザイン指定
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


}