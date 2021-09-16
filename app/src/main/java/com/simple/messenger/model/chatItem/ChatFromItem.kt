package com.simple.messenger.model.chatItem

import android.net.Uri
import android.view.View
import com.simple.messenger.R
import com.simple.messenger.databinding.ChatFromRowBinding
import com.simple.messenger.model.User
import com.simple.messenger.viewModel.UserPageViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class ChatFromItem(private val imageUri: String, val text: String, val user: User): BindableItem<ChatFromRowBinding>() {
    override fun bind(viewBinding: ChatFromRowBinding, position: Int) {

        if (imageUri == "") {  // 画像がない場合

            viewBinding.sendImage.visibility = View.GONE

            viewBinding.textView.text = text
            val uri = Uri.parse(user.profileImageUri)
            val userImageView = viewBinding.imageviewChatFromRow

            Picasso.get().load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(userImageView)

        } else {  // 画像がある場合

            if (text == "") {
                viewBinding.textView.visibility = View.GONE
            } else {
                viewBinding.textView.text = text
                viewBinding.textView.width = viewBinding.textView.maxWidth
                viewBinding.textView.setBackgroundResource(R.drawable.image_description_text)
            }

            // 送信画像
            val sendImageUri = Uri.parse(imageUri)
            val sendImageView = viewBinding.sendImage
            Picasso.get().load(sendImageUri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(sendImageView)
            // スクロールすると画像が消えてしまうバグ解消法
            sendImageView.visibility = View.VISIBLE

            sendImageView.setOnClickListener {
                UserPageViewModel.showImage(sendImageView, imageUri)
            }

            // ユーザーイメージ画像
            val uri = Uri.parse(user.profileImageUri)
            val userImageView = viewBinding.imageviewChatFromRow

            Picasso.get().load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(userImageView)

        }

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
