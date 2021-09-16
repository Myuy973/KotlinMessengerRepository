package com.simple.messenger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simple.messenger.R
import com.simple.messenger.databinding.FragmentLoginBinding
import com.simple.messenger.model.EventObserver
import com.simple.messenger.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var activity: AppCompatActivity

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = getActivity() as AppCompatActivity

        viewModel.googleSignInClient = GoogleSignIn.getClient(activity, viewModel.gso)

        binding.loginViewModel = viewModel
        binding.lifecycleOwner = activity

        loginToolbar.title = activity.getString(R.string.app_name)

        login_button.setOnClickListener {
            viewModel.performRegister("Login")
        }

        back_to_register_textView.setOnClickListener {
            findNavController().navigate(R.id.action_Login_to_Register)
        }
        google_login_button.setOnClickListener {
            googleSignin()
        }

        login_progressBar.setOnTouchListener { _, _ -> true }


        viewModel.loginPageEvent.observe(viewLifecycleOwner, EventObserver { destination: String ->
            when (destination) {
                "enter" -> {
                    hideKeyboard()
                    findNavController().navigate(R.id.action_Login_to_LatestMessages)
                }
                "enterWithSNS" -> {
                    hideKeyboard()
                    val action =
                        LoginFragmentDirections.actionLoginToLatestMessages(true)
                    findNavController().navigate(action)

                }
            }
        })


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        viewModel.googleSigninFunction(data, "Login", activity)
    }

    private fun googleSignin() {
        val intent = viewModel.googleSignInClient.signInIntent
        val requestCode = viewModel.GOOGLE_SIGNIN
        startActivityForResult(intent, requestCode)
    }

    private fun hideKeyboard() {
        val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

    }

}