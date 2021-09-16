package com.simple.friendship_messenger.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import com.simple.friendship_messenger.R
import com.simple.friendship_messenger.databinding.ActivityShowBinding
import com.simple.friendship_messenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show.*

class ShowActivity : AppCompatActivity() {

    val VIEW_NAME_HEADER_IMAGE = "showImage"
    lateinit var binding: ActivityShowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show)

        val imageUri = intent.getStringExtra(UserPageViewModel(application).IMAGE_SHOW) ?: ""

        ViewCompat.setTransitionName(show_image_preview, VIEW_NAME_HEADER_IMAGE)
        val uri = Uri.parse(imageUri)
        Picasso.get().load(uri).into(show_image_preview)

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