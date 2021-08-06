package com.example.kotlinmessenger.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityRegisterBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.internal.Util
import java.util.*

class RegisterActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRegisterBinding

    private val viewModel : LoginViewModel by viewModels()

    private lateinit var auth: FirebaseAuth

    companion object {
        lateinit var loginUser: User
        val RC_SIGN_IN = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register)
        viewModel.googleSignInClient = GoogleSignIn.getClient(this, viewModel.gso)

        binding.loginviewModel = viewModel
        binding.lifecycleOwner = this


        selectphoto_button_register.setOnClickListener {
            Log.d("value", "try  to show photo selector")
            viewModel.inputImage(this)
        }
        register_button_register.setOnClickListener {
            viewModel.performRegister(this)
        }

        already_have_an_account_textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        google_signin_button.setOnClickListener {
            viewModel.googleSignin(this)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

//        Log.d("value", "requestCode: $requestCode, resultCode: $resultCode, data: $data")
        if (requestCode == viewModel.IMAGE_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("value", "Photo was selected")
            
            viewModel.imageSetFunction(data, this)

        } else if (requestCode == viewModel.GOOGLE_SIGNIN) {

            viewModel.googleSigninFunction(data, this)

        }

    }

}