package com.example.kotlinmessenger.model.ChatItem

import android.app.Activity
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.example.kotlinmessenger.model.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class ChatToItem(val imageUri: String, val text: String, val user: User, activity: Activity): BindableItem<ChatToRowBinding>() {
    override fun bind(viewBinding: ChatToRowBinding, position: Int) {

        if (imageUri == "") {

            viewBinding.sendImage.visibility = View.INVISIBLE

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

            val sendImageView = viewBinding.sendImage
            Picasso.get().load(imageUri).into(sendImageView)

            val uri = user.profileImageUri
            val userImageView = viewBinding.imageviewChatToRow
            Picasso.get().load(uri).into(userImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}