package com.example.kotlinmessenger.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.ActivityNewMessageBinding
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {

    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var binding: ActivityNewMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_message)

        recyclerview_newmessage.adapter = viewModel.NewMessageAdapter.value
        recyclerview_newmessage.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        supportActionBar?.title = "Select User"

        viewModel.fetchUserFriends(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.add_friend -> {

                val dialog = AlertDialog.Builder(this)
                val view = View.inflate(this, R.layout.dialog_input_friend_id, null)
                dialog.setView(view)
                    .setPositiveButton("追加") { dialog, which ->
                        val inputUid = view.findViewById<EditText>(R.id.input_friend_user_id_text).text.toString()
                        Log.d("value", "input: $inputUid")
                        viewModel.addFriendFunction(inputUid, this)
                    }
                    .setNegativeButton("キャンセル", null)
                    .show()


            }
            R.id.delete_friend -> {
                val friendNameList: Array<String> = viewModel.friendList.map {
                    it.userName
                }.toTypedArray()
                val deleteFriendNumber = mutableListOf<Int>()


                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("削除するユーザーを選んでください")
                    .setMultiChoiceItems(friendNameList, null) { dialog, which, isChecked ->
                        if (isChecked) {
                            Log.d("value", "$which selected")
                            Log.d("value", "list: $deleteFriendNumber")
                            deleteFriendNumber += which
                        } else if (deleteFriendNumber.contains(which)) {
                            deleteFriendNumber.remove(which)
                        }
                    }
                    .setPositiveButton("削除") { dialog, which ->
                        Log.d("value", "delete function: $deleteFriendNumber")
                        viewModel.deleteFriendFunction(deleteFriendNumber, this)
                    }
                    .setNeutralButton("キャンセル", null)
                    .show()

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_new_message, menu)
        return super.onCreateOptionsMenu(menu)
    }

}