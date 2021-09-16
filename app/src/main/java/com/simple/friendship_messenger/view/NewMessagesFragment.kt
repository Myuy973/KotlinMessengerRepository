package com.simple.friendship_messenger.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.simple.friendship_messenger.R
import com.simple.friendship_messenger.databinding.FragmentNewMessagesBinding
import com.simple.friendship_messenger.model.UserItem
import com.simple.friendship_messenger.viewModel.UserPageViewModel
import kotlinx.android.synthetic.main.fragment_chat_log.*
import kotlinx.android.synthetic.main.fragment_new_messages.*

class NewMessagesFragment : Fragment() {

    private var _binding: FragmentNewMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var activity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_messages, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = getActivity() as AppCompatActivity
        new_message_toolbar.title = activity.getString(R.string.new_messages_title)
        new_message_toolbar.inflateMenu(R.menu.nav_new_message)
        new_message_toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_small_back_button)
        new_message_toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_NewMessages_to_LatestMessages)
        }

        viewModel.newMessageAdapter.value?.setOnItemClickListener { item, _ ->
            val user = (item as UserItem).user
            val action =
                NewMessagesFragmentDirections.actionNewMessagesToChatLog(user)
            findNavController().navigate(action)
        }
        recyclerview_newmessage.adapter = viewModel.newMessageAdapter.value
        recyclerview_newmessage.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))


        viewModel.fetchUserFriends()

        new_message_toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.add_friend -> {
                    val dialog = AlertDialog.Builder(activity, R.style.MyDialogTheme)
                    val view = View.inflate(activity, R.layout.dialog_input_friend_id, null)
                    dialog.setView(view)
                        .setPositiveButton("追加") { _, _ ->
                            val inputUid = view.findViewById<EditText>(R.id.input_friend_user_id_text).text.toString()
                            viewModel.addFriendFunction(inputUid)
                        }
                        .setNegativeButton("キャンセル", null)
                    val customDialog = dialog.create()
                    customDialog.show()
                    // positive, negativeボタンの色指定
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getColor(R.color.black))
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getColor(R.color.dialog_cancel))
                }

                R.id.delete_friend -> {
                    // フレンド名リスト作成
                    val friendNameList: Array<String> = viewModel.friendList.map {
                        it.userName
                    }.toTypedArray()
                    val deleteFriendNumber = mutableListOf<Int>()
                    val dialog = AlertDialog.Builder(activity, R.style.MyDialogTheme)
                    dialog.setTitle("削除するユーザーを選んでください")
                        .setMultiChoiceItems(friendNameList, null) { _, which, isChecked ->
                            if (isChecked) {
                                deleteFriendNumber += which
                            } else if (deleteFriendNumber.contains(which)) {
                                deleteFriendNumber.remove(which)
                            }
                        }
                        .setPositiveButton("削除") { _, _ ->
                            viewModel.deleteFriendFunction(deleteFriendNumber)
                        }
                        .setNeutralButton("キャンセル", null)
                    val customDialog = dialog.create()
                    customDialog.show()
                    // positive, neutralボタンの色指定
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getColor(R.color.black))
                    customDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(activity.getColor(R.color.dialog_cancel))
                }
            }

            return@setOnMenuItemClickListener false
        }


    }

}