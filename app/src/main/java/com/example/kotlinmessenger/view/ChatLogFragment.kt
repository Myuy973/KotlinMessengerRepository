package com.example.kotlinmessenger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FragmentChatLogBinding
import com.example.kotlinmessenger.viewModel.UserPageViewModel
import kotlinx.android.synthetic.main.fragment_chat_log.*


class ChatLogFragment : Fragment() {

    private var _binding: FragmentChatLogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var activity: AppCompatActivity
    private lateinit var toUser: com.example.kotlinmessenger.model.User
    private val args: ChatLogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity()
                .window
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_log, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = getActivity() as AppCompatActivity
        binding.userPageViewModel = viewModel
        binding.lifecycleOwner = activity


        // チャット相手の取得、タイトルにセット
        toUser = args.partnerUser
        chat_log_toolbar.title = toUser.userName
        chat_log_toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_small_back_button)
        chat_log_toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_ChatLog_to_NewMessages)
        }


        // チャットデータ収集
        viewModel.listenForMessages(toUser, activity)
        recyclerview_chat_log.adapter = viewModel.ChatLogAdapter.value
        recyclerview_chat_log.setHasFixedSize(true)
        recyclerview_chat_log.setItemViewCacheSize(20)



        send_button_chat_log.setOnClickListener {
//            Log.d("log", "attempt to send message...")
            viewModel.performSendMessage(activity)
        }
        image_select_button.setOnClickListener {
            imageSelecterStart()
        }

        UserPageViewModel.showImageData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                changeToShowActivity(data.first, data.second)
            }
        }

        chat_log_toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId)  {
                android.R.id.home -> {
                    viewModel.eventlistenerFinish()
                    activity.onBackPressed()
                }
            }

            return@setOnMenuItemClickListener false
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.imageSelectedFunction(data, activity)
    }


    private fun imageSelecterStart() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, viewModel.IMAGE_SELECT)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("log", "chat log is destroy")
        viewModel.eventlistenerFinish()
    }


        @SuppressWarnings("unchecked")
    fun changeToShowActivity(imageView: View, imageUri: String) {

        imageView.visibility = View.VISIBLE

        val intent = Intent(activity, ShowActivity::class.java)
//        Log.d("log", "imageuri: $imageUri")
        intent.putExtra(viewModel.IMAGE_SHOW, imageUri)

        val activityOptions: ActivityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    Pair(imageView, ShowActivity().VIEW_NAME_HEADER_IMAGE)
            )

        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())

    }



}