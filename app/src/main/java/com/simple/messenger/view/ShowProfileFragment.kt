package com.simple.messenger.view

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.navigation.fragment.findNavController
import com.simple.messenger.R
import com.simple.messenger.databinding.FragmentShowProfileBinding
import com.simple.messenger.viewModel.UserPageViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_chat_log.*
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ShowProfileFragment : Fragment() {

    private var _binding: FragmentShowProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserPageViewModel by viewModels()
    private lateinit var activity: AppCompatActivity

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
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_show_profile, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = getActivity() as AppCompatActivity
        show_profile_toolbar.title = activity.getString(R.string.show_profile_title)
        show_profile_toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_small_back_button)
        show_profile_toolbar.setNavigationOnClickListener {
            (activity as MessengerActivity).hideKeyboard()
            findNavController().navigate(R.id.action_ShowProfile_to_LatestMessages)
        }


        binding.viewModel = viewModel
        binding.lifecycleOwner = activity

        // editUserNameText, editUserEmailText, editUserPassText チェッカー起動
        viewModel.setUpCheck()

        // ユーザー情報をLiveDataへ
        viewModel.userInfoDisplay()

        val currentUserData = UserPageViewModel.currentUser
        Picasso.get().load(viewModel.editImageUri.value).into(profile_user_imageview)

        // profile image change
        image_change_icon.setOnClickListener { profileImageSelect() }

        // profile user id
        profile_user_id_text.text = currentUserData.uid

        user_id_clip_button.setOnClickListener {
            try {
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("user_id", currentUserData.uid)
                clipboard.setPrimaryClip(clip)
                UserPageViewModel.printToast("テキストをコピーしました。")
            } catch (e: Exception) {
                UserPageViewModel.printToast("テキストコピーに失敗しました。")
            }
        }


        updataToProfileButton.setOnClickListener { viewModel.userProfileUpdate() }

        // email, pass, imageそれぞれの更新処理が終わっているかチェック
        listOf( viewModel.emailUpdateProcess,
                viewModel.passUpdateProcess,
                viewModel.imageUpdateProcess).forEach { liveData ->
                    liveData.asFlow()
                        .onEach { viewModel.userdataUpdate() }
                        .launchIn(GlobalScope)
        }


        update_progressBar.setOnTouchListener { _, _ -> true }

        show_profile_toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                android.R.id.home -> {
                    activity.onBackPressed()
                }
            }
            return@setOnMenuItemClickListener false
        }

    }


    private fun profileImageSelect() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, viewModel.PROFILE_IMAGE_CHANGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == viewModel.PROFILE_IMAGE_CHANGE && resultCode == Activity.RESULT_OK) {
            viewModel.profileImageChange(data, activity.contentResolver)
        }
    }

}