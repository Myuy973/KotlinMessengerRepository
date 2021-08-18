package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityLoginBinding
import com.example.kotlinmessenger.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel.googleSignInClient = GoogleSignIn.getClient(this, viewModel.gso)

        binding.loginViewModel = viewModel
        binding.lifecycleOwner = this

        login_button.setOnClickListener {
            try {
                viewModel.performRegister(this)
            } catch (e: Exception) {
                Log.d("value",  "error: ${e.printStackTrace()}")
            }
        }

        back_to_register_textView.setOnClickListener {
            finish()
        }
        google_login_button.setOnClickListener {
            try {
                viewModel.googleSignin(this)
            } catch (e: Exception) {
                Log.d("value",  "error: ${e.printStackTrace()}")
            }
        }

        overridePendingTransition(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.googleSigninFunction(data, this)
    }


}