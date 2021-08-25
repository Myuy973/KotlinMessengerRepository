package com.example.kotlinmessenger.model.ChatItem

import android.app.Activity
import android.opengl.Visibility
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ChatFromRowBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class ChatFromItem(val imageUri: String, val text: String, val user: User, val activity: Activity): BindableItem<ChatFromRowBinding>() {
    override fun bind(viewBinding: ChatFromRowBinding, position: Int) {

        if (imageUri == "") {

            viewBinding.sendImage.visibility = View.INVISIBLE

            viewBinding.textView.text = text
            val uri = user.profileImageUri
            val userImageView = viewBinding.imageviewChatFromRow
            Picasso.get().load(uri).into(userImageView)
        } else {
            if (text == "") {
                viewBinding.textView.visibility = View.GONE
            } else {
                viewBinding.textView.text = text
                viewBinding.textView.width = viewBinding.textView.maxWidth
                viewBinding.textView.setBackgroundResource(R.drawable.image_description_text)
            }

            val sendImageView = viewBinding.sendImage
            Picasso.get().load(imageUri).into(sendImageView)
            sendImageView.setOnClickListener {
                UserPageViewModel().changeToShowActivity(sendImageView as View, imageUri, activity)
            }

            val uri = user.profileImageUri
            val userImageView = viewBinding.imageviewChatFromRow
            Picasso.get().load(uri).into(userImageView)
        }

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
