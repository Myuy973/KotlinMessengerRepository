package com.simple.friendship_messenger.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.simple.friendship_messenger.R
import com.simple.friendship_messenger.databinding.FragmentLatestMessagesBinding
import com.simple.friendship_messenger.model.EventObserver
import com.simple.friendship_messenger.model.LatestMessageRow
import com.simple.friendship_messenger.viewModel.UserPageViewModel
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesFragment : Fragment() {

    private val viewModel: UserPageViewModel by viewModels()
    private var _binding: FragmentLatestMessagesBinding? = null
    private val binding get() = _binding!!
    private val args: LatestMessagesFragmentArgs by navArgs()
    private var snsLogin: Boolean = false
    private lateinit var activity: AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_latest_messages, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        activity = getActivity() as AppCompatActivity
        snsLogin = args.snsLoginType

        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        latest_messages_toolbar.title = activity.getString(R.string.latest_messages_title)
        latest_messages_toolbar.inflateMenu(R.menu.nav_menu)

        recycler_latest_messages.adapter = viewModel.latestMessagesAdapter.value
        // カード間にボーダー
        recycler_latest_messages.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        viewModel.latestMessagesAdapter.value?.setOnItemClickListener { item, itemView ->
            itemView.newMessageIAnimIcon.visibility = View.INVISIBLE

            val rowUserData = (item as LatestMessageRow).chatPartnerUser!!
            val action =
                LatestMessagesFragmentDirections.actionLatestMessagesToChatLog(rowUserData)
            findNavController().navigate(action)
        }

        // ログインしているかどうか
        if (viewModel.verifyUserIsLoggedIn()) {
            // ログインしてるユーザーデータを取得
            viewModel.fetchCurrentUser()
            // ユーザーデータを表示
            viewModel.listenForLatestMessages()
        }


    // Toolbarでアイテムが押されたときのアクション
        latest_messages_toolbar.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.menu_new_message -> {
                    findNavController().navigate(R.id.action_LatestMessages_to_NewMessages)
                }
                R.id.user_profile -> {
                    findNavController().navigate(R.id.action_LatestMessages_to_ShowProfile)
                }
                R.id.menu_sign_out -> {
                    AuthUI.getInstance().signOut(activity).addOnSuccessListener {
                        findNavController().navigate(R.id.action_LatestMessages_to_Register)
                    }
                }
            }

            return@setOnMenuItemClickListener false

        }

        // 画面遷移
        viewModel.latestMessagePageEvent.observe(viewLifecycleOwner, EventObserver { destination ->
            when(destination) {
                "toRegister" -> {
                    findNavController().navigate(R.id.action_LatestMessages_to_Register)
                }
            }
        })

        // ローディング画面　タップ無効化
        latest_messages_progressBar.setOnTouchListener { _, _ -> true  }



    }
}