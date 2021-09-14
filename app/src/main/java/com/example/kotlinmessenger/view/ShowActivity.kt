package com.example.kotlinmessenger.view

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityShowBinding
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show.*

class ShowActivity : AppCompatActivity() {

    val VIEW_NAME_HEADER_IMAGE = "showImage"
    lateinit var binding: ActivityShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show)

        val imageUri = intent.getStringExtra(UserPageViewModel(application).IMAGE_SHOW) ?: ""
//        Log.d("log", "show Activity imageuri: $imageUri")

        ViewCompat.setTransitionName(show_image_preview, VIEW_NAME_HEADER_IMAGE)
        val uri = Uri.parse(imageUri)
        Picasso.get().load(uri).into(show_image_preview)
//        show_image_preview.setImageURI(uri)
//        Log.d("log", "show Activity visibility : ${show_image_preview.visibility}")

        show_activity_container.setOnClickListener { onBackPressed() }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
                R.anim.anim_activity_close,
                R.anim.anim_activity_open
        )
        UserPageViewModel.hideImage()
    }

}