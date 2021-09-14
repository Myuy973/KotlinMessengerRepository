package com.example.kotlinmessenger.model.ChatItem

import android.net.Uri
import android.view.View
import androidx.core.util.Pair
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ChatToRowBinding
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class ChatToItem(val imageUri: String, val text: String, val user: User): BindableItem<ChatToRowBinding>() {
    override fun bind(viewBinding: ChatToRowBinding, position: Int) {

        if (imageUri == "") {

            viewBinding.sendImage.visibility = View.GONE

            viewBinding.textView.text = text
            val uri = Uri.parse(user.profileImageUri)
            val userImageView = viewBinding.imageviewChatToRow
//            userImageView.setImageURI(uri)
            Picasso.get().load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(userImageView)
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
//            sendImageView.setImageURI(sendImageUri)
            Picasso.get().load(sendImageUri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(sendImageView)
            // スクロールすると画像が消えてしまうバグ解消法
            sendImageView.visibility = View.VISIBLE

            sendImageView.setOnClickListener {
                UserPageViewModel.showImageData.value = Pair(sendImageView, imageUri)
            }

            val uri = Uri.parse(user.profileImageUri)
            val userImageView = viewBinding.imageviewChatToRow
//            userImageView.setImageURI(uri)
            Picasso.get().load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(userImageView)
        }
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}