package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.loginButton.setOnClickListener {
            performLogin()
        }

        binding.backToRegisterTextView.setOnClickListener {
            finish()
        }


    }

    private fun performLogin() {
        val email = binding.emailEdittextLogin.text.toString()
        val pass = binding.passwordEdittextLogin.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter text in text/ps", Toast.LENGTH_LONG)
                .show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                val intent = Intent(this, LatestMessagesActivity::class.java)
                // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)

                Toast.makeText(this, "Successfully Login", Toast.LENGTH_LONG)
                    .show()
            }

    }


}