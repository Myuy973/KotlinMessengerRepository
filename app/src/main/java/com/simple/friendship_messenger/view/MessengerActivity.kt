package com.simple.friendship_messenger.view

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simple.friendship_messenger.databinding.ActivityMessengerBinding
import com.simple.friendship_messenger.viewModel.LoginViewModel
import com.simple.friendship_messenger.viewModel.UserPageViewModel

class MessengerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoginViewModel.loginToastText.observe(this) { text ->
            printToast(text)
        }
        UserPageViewModel.userPageToastText.observe(this) { text ->
            printToast(text)
        }

    }

    private fun printToast(text: String) {
        if (text.isNotEmpty()) Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }



}