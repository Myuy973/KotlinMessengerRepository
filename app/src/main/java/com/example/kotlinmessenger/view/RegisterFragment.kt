package com.example.kotlinmessenger.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.databinding.FragmentRegisterBinding
import com.example.kotlinmessenger.model.EventObserver
import com.example.kotlinmessenger.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel : LoginViewModel by viewModels()
    private lateinit var activity: AppCompatActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = getActivity() as AppCompatActivity
        viewModel.googleSignInClient = GoogleSignIn.getClient(activity, viewModel.gso)

        binding.loginviewModel = viewModel
        binding.lifecycleOwner = activity

        registerToolbar.title = activity.getString(R.string.app_name)

        selectphoto_button_register.setOnClickListener {
            inputImage()
        }

        register_button_register.setOnClickListener {
            viewModel.performRegister("Register")
        }

        already_have_an_account_textView.setOnClickListener {
            findNavController().navigate(R.id.action_Register_to_Login)
        }
        google_signin_button.setOnClickListener {
            googleSignin()
        }

        // ローディング画面　タップ無効化
        signin_progressBar.setOnTouchListener { _, _ -> true }

        // 画面遷移
        viewModel.registerPageEvent.observe(viewLifecycleOwner, EventObserver { destination: String ->
            when (destination) {
                "enter" -> {
                    (activity as MessengerActivity).hideKeyboard()
                    findNavController().navigate(R.id.action_Register_to_LatestMessages)
                }
                "enterWithSNS" -> {
                    (activity as MessengerActivity).hideKeyboard()
                    val action =
                        RegisterFragmentDirections.actionRegisterToLatestMessages(true)
                    findNavController().navigate(action)

                }
            }
        })


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == viewModel.IMAGE_INPUT && resultCode == Activity.RESULT_OK && data != null) {

            viewModel.imageSetFunction(data, activity.contentResolver)

        } else if (requestCode == viewModel.GOOGLE_SIGNIN) {

            viewModel.googleSigninFunction(data, "Register", activity)

        }

    }

    private fun inputImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, viewModel.IMAGE_INPUT)
    }

    private fun googleSignin() {
        val intent = viewModel.googleSignInClient.signInIntent
        val requestCode = viewModel.GOOGLE_SIGNIN
        startActivityForResult(intent, requestCode)
    }







}