package com.example.kotlinmessenger.view

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityEntranceBinding
import com.example.kotlinmessenger.model.EventObserver
import com.example.kotlinmessenger.viewModel.LoginViewModel
import java.security.AccessController

class EntranceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEntranceBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var naviController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntranceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        naviController = findNavController(R.id.nav_host_fragment)

        LoginViewModel.entrancePageEvent.observe(this, EventObserver { destination ->
            Log.d("log", "move type: $destination")
            when (destination) {
                "enter" -> {
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.putExtra("fromActivity", "SigninOrLogin")
                    intent.putExtra("snsLogin", false)
                    // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                "enterWithSNS" -> {
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.putExtra("fromActivity", "SigninOrLogin")
                    intent.putExtra("snsLogin", true)
                    // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                }
            }
        })

        LoginViewModel.entranceToastText.observe(this) { text ->
            Log.d("log", "log out put")
            if (text.isNotEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }


    }

}