package com.example.kotlinmessenger.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asFlow
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityShowProfileBinding
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Exception

class ShowProfileActivity : AppCompatActivity() {

    val viewModel: UserPageViewModel by viewModels()
    lateinit var binding: ActivityShowProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_profile)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val currentUserData = UserPageViewModel.currentUser

        // profile imageview
        Picasso.get().load(currentUserData.profileImageUri).into(binding.profileUserImageview)
        // profile image change
        image_change_icon.setOnClickListener { profileImageSelect() }

        // profile user id
        profile_user_id_text.text = currentUserData.uid

        user_id_clip_button.setOnClickListener {
            try {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("user_id", currentUserData.uid)
                clipboard.setPrimaryClip(clip)
                viewModel.printToast("テキストをコピーしました。", this)
            } catch (e: Exception) {
                viewModel.printToast("テキストコピーに失敗しました。", this)
            }
        }

        viewModel.setUpchecker()

        // ユーザー情報をLiveDataへ
        viewModel.userInfoDisplay()

        updataToProfileButton.setOnClickListener { viewModel.userProfileUpdate(this) }

        // email, pass, imageそれぞれの更新処理が終わっているかチェック
        listOf( viewModel.emailUpdateProcess,
                viewModel.passUpdateProcess,
                viewModel.imageUpdateProcess).forEach { liveData ->
                    liveData.asFlow()
                        .onEach { viewModel.userdataUpdate(this) }
                        .launchIn(GlobalScope)
        }


        update_progressBar.setOnTouchListener { _, _ -> true }

    }

    private fun profileImageSelect() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, viewModel.PROFILE_IMAGE_CHANGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == viewModel.PROFILE_IMAGE_CHANGE && resultCode == Activity.RESULT_OK) {
            viewModel.profileImageChange(data, this)
        }
    }



}