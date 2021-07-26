package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.databinding.ActivityNewMessageBinding
import com.example.kotlinmessenger.databinding.UserRowNewMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.databinding.BindableItem
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        fetchUsers()

    }

    //??????
    companion object {
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupieAdapter()
                snapshot.children.forEach {
                    Log.d("value", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }
                recyclerview_newmessage.adapter = adapter

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem
                    Log.d("value", "item: $item")
                    Log.d("value", "userItem: $userItem")
                    Log.d("value", "user: ${userItem.user}")

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

}

class UserItem(val user: User): BindableItem<UserRowNewMessageBinding>() {

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewBinding: UserRowNewMessageBinding, position: Int) {
        viewBinding.usernameTextviewNewMessage.text = user.username

        Picasso.get().load(user.profileImageUri)
            .into(viewBinding.imageView)

    }

}
