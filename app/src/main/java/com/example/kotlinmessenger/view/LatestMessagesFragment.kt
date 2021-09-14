package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FragmentLatestMessagesBinding
import com.example.kotlinmessenger.model.EventObserver
import com.example.kotlinmessenger.model.LatestMessageRow
import com.example.kotlinmessenger.viewModel.UserPageViewModel
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
    ): View? {
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

        recycler_latest_messages.adapter = viewModel.LatestMessagesAdapter.value
        // カード間にボーダー
        recycler_latest_messages.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        viewModel.LatestMessagesAdapter.value?.setOnItemClickListener { item, view ->
            view.newMessageIAnimIcon.visibility = View.INVISIBLE

            val rowUserData = (item as LatestMessageRow).chatPartnerUser!!
            val action =
                LatestMessagesFragmentDirections.actionLatestMessagesToChatLog(rowUserData)
            findNavController().navigate(action)
        }

        // ログインしているかどうか
        if (viewModel.verifyUserIsLoggedIn()) {
            viewModel.fetchCurrentUser()
            viewModel.listenForLatestMessages()
        }


    // Toolbarでアイテムが押されたときのアクション
        latest_messages_toolbar.setOnMenuItemClickListener { item ->

            when (item.itemId) {
                R.id.menu_new_message -> {
                    findNavController().navigate(R.id.action_LatestMessages_to_NewMessages)
                }
                R.id.user_profile -> {
                    val action =
                        LatestMessagesFragmentDirections.actionLatestMessagesToShowProfile(snsLogin)
                    findNavController().navigate(action)
                }
                R.id.menu_sign_out -> {
                    AuthUI.getInstance().signOut(activity).addOnSuccessListener {
//                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        findNavController().navigate(R.id.action_LatestMessages_to_Register)
                    }.addOnFailureListener {
//                    Log.d("log", "error: ${it.printStackTrace()}")
                    }
                }
            }

            return@setOnMenuItemClickListener false

        }

        viewModel.latestMessagePageEvent.observe(viewLifecycleOwner, EventObserver { destination ->
            when(destination) {
                "toRegister" -> {
                    findNavController().navigate(R.id.action_LatestMessages_to_Register)
                }
            }
        })

        latest_messages_progressBar.setOnTouchListener { _, _ -> true  }



    }



//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        when (item.itemId) {
//            R.id.menu_new_message -> {
//                val intent = Intent(activity, NewMessageActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.user_profile -> {
//                val intent = Intent(activity, ShowProfileActivity::class.java)
//                intent.putExtra("snsLogin", snsLogin)
//                startActivity(intent)
//            }
//            R.id.menu_sign_out -> {
//                AuthUI.getInstance().signOut(activity).addOnSuccessListener {
//                    val intent = Intent(activity, MessengerActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                }.addOnFailureListener {
////                    Log.d("log", "error: ${it.printStackTrace()}")
//                }
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    // アクションバーのデザイン指定
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.nav_menu, menu)
//        return super.onCreateOptionsMenu(menu)
//    }


}