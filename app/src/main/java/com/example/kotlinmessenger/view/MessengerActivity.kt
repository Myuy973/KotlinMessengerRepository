package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityMessengerBinding
import com.example.kotlinmessenger.model.EventObserver
import com.example.kotlinmessenger.viewModel.LoginViewModel
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import kotlinx.android.synthetic.main.content_entrance.*
import kotlinx.android.synthetic.main.fragment_chat_log.*
import kotlinx.android.synthetic.main.fragment_new_messages.*
import kotlinx.android.synthetic.main.fragment_show_profile.*

class MessengerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var naviController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        naviController = findNavController(R.id.nav_host_fragment)
//        activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        LoginViewModel.loginToastText.observe(this) { text ->
            printToast(text)
        }
        UserPageViewModel.userPageToastText.observe(this) { text ->
            printToast(text)
        }

    }

    fun printToast(text: String) {
        Log.d("log", "log out put")
        if (text.isNotEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}