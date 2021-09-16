package com.simple.messenger.model

import com.simple.messenger.R
import com.simple.messenger.databinding.UserRowNewMessageBinding
import com.squareup.picasso.Picasso
import com.xwray.groupie.databinding.BindableItem

class UserItem(val user: User): BindableItem<UserRowNewMessageBinding>() {

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewBinding: UserRowNewMessageBinding, position: Int) {
        viewBinding.usernameTextviewNewMessage.text = user.userName

        Picasso.get().load(user.profileImageUri)
                .into(viewBinding.imageView)

    }

}
