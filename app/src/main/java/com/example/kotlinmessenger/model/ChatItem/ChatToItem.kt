package com.example.kotlinmessenger.model.ChatItem

import android.app.Activity
import android.net.Uri
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class ChatToItem(private val imageUri: String, val text: String, val user: User, val activity: Activity): BindableItem<ChatToRowBinding>() {
    override fun bind(viewBinding: ChatToRowBinding, position: Int) {

        if (imageUri == "") {

            viewBinding.sendImage.visibility = View.GONE

            viewBinding.textView.text = text
            val uri = user.profileImageUri
            val userImageView = viewBinding.imageviewChatToRow
            Picasso.get().load(uri).into(userImageView)
        } else {
            if (text == "") {
                viewBinding.textView.visibility = View.GONE
            } else {
                viewBinding.textView.text = text
                viewBinding.textView.width = viewBinding.textView.maxWidth
                viewBinding.textView.setBackgroundResource(R.drawable.image_description_text)
            }

            val sendImageUri = Uri.parse(imageUri)
            val sendImageView = viewBinding.sendImage
            sendImageView.setImageURI(sendImageUri)
            sendImageView.setOnClickListener {
                UserPageViewModel().changeToShowActivity(sendImageView as View, imageUri, activity)
            }

            val uri = user.profileImageUri
            val userImageView = viewBinding.imageviewChatToRow
            userImageView.setImageURI(uri)
        //            Picasso.get().load(uri).into(userImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}