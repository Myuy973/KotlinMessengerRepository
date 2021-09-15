package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.model.Event
import com.example.kotlinmessenger.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class LoginViewModel(
        private val myApplication: Application
) : AndroidViewModel(myApplication) {

    val IMAGE_INPUT = 1
    val GOOGLE_SIGNIN = 2

    // messenger page event
    val registerPageEvent = MutableLiveData<Event<String>>()
    // messenger page event
    val loginPageEvent = MutableLiveData<Event<String>>()

    // progressBar visibility
    val progressbarType = MutableLiveData<Int>(View.GONE)

    // -------------------- email signin -----------------------------

    val userImage = MutableLiveData<Boolean>(false)
    val userName = MutableLiveData<String>("")
    val userEmail = MutableLiveData<String>("")
    val userPassword = MutableLiveData<String>("")
    val signinButtonType = MutableLiveData<Boolean>(false)
    var signErrorMessage: MutableList<String> = mutableListOf()

    val loginUserEmail = MutableLiveData<String>("")
    val loginUserPass = MutableLiveData<String>("")
    val loginButtonType = MutableLiveData<Boolean>(false)
    var loginErrorMessage: MutableList<String> = mutableListOf()



    // ------------------- google signin ----------------------------------------------

    lateinit var googleSignInClient: GoogleSignInClient
    private var auth: FirebaseAuth = Firebase.auth

    var buttonAlpha = MutableLiveData<Float>(1f)
    private var selectedPhotoUri: Uri? = null
    var bitmap = MutableLiveData<Bitmap?>(null)

    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("693890654310-4h5vpi1psul17adjt04cmqdsit9tpb8g.apps.googleusercontent.com")
            .requestEmail()
            .build()
    //-------------------------------------------------------

    companion object {
        // messenger toast
        val loginToastText = MutableLiveData<String>("")
    }

    init {

        progressbarType.value = View.GONE

        // それぞれ変更されたら全項目が入力済みかチェック
        listOf(userName, userEmail, userPassword).forEach { liveData ->
            liveData.asFlow()
                    .onEach { checkSigninItem() }
                    .launchIn(viewModelScope)
        }

        listOf(loginUserEmail, loginUserPass).forEach { liveData ->
            liveData.asFlow()
                    .onEach { checkLoginItem() }
                    .launchIn(viewModelScope)
        }

    }

    // それぞれの入力内容をチェック(Register)
    private fun checkSigninItem() {

        if (!userName.value?.isEmpty()!!) {
            signErrorMessage.remove(myApplication.getString(R.string.name_empty))
        } else if (!signErrorMessage.contains(myApplication.getString(R.string.name_empty))) {
            signErrorMessage.add(myApplication.getString(R.string.name_empty))
        }

        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (emailPattern.matches(userEmail.value.toString())) {
            signErrorMessage.remove(myApplication.getString(R.string.email_input_error))
        } else if (!signErrorMessage.contains(myApplication.getString(R.string.email_input_error))) {
            signErrorMessage.add(myApplication.getString(R.string.email_input_error))
        }


        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (passPattern.matches(userPassword.value.toString())) {
            signErrorMessage.remove(myApplication.getString(R.string.pass_mismatch))
        } else if (!signErrorMessage.contains(myApplication.getString(R.string.pass_mismatch))) {
            signErrorMessage.add(myApplication.getString(R.string.pass_mismatch))
        }


        signinButtonType.value =
                userImage.value!! &&
                !userName.value?.isEmpty()!! &&
                !userEmail.value?.isEmpty()!! &&
                !userPassword.value?.isEmpty()!!

    }

    // それぞれの入力内容をチェック(Login)
    private fun checkLoginItem() {

        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (emailPattern.matches(loginUserEmail.value.toString())) {
            loginErrorMessage.remove(myApplication.getString(R.string.email_input_error))
        } else if (!loginErrorMessage.contains(myApplication.getString(R.string.email_input_error))) {
            loginErrorMessage.add(myApplication.getString(R.string.email_input_error))
        }


        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (passPattern.matches(loginUserPass.value.toString())) {
            loginErrorMessage.remove(myApplication.getString(R.string.pass_mismatch))
        } else if (!loginErrorMessage.contains(myApplication.getString(R.string.pass_mismatch))) {
            loginErrorMessage.add(myApplication.getString(R.string.pass_mismatch))
        }



        loginButtonType.value =
                !loginUserEmail.value?.isEmpty()!! &&
                !loginUserPass.value?.isEmpty()!!

    }


    // ----------------- common Signin Function -------------------------------------------

    // 作成したユーザーデータをデータベースへ保存
    // LatestMessageへ移動
    private fun saveUserToFirebaseDatabase(profileImageUri: String,
                                           userName: String,
                                           userEmail: String,
                                           snsLogin: Boolean,
                                           fromFragment: String) {


        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = Firebase.database.getReference("users/$uid")

        val user = User(uid, userName, userEmail,  profileImageUri, snsLogin)

        ref.setValue(user).addOnSuccessListener {
            loginToastText.value = "ようこそ ${user.userName}さん"
            progressbarType.value = View.GONE

            val event = if (snsLogin) Event("enterWithSNS") else Event("enter")
            when (fromFragment) {
                "Login" -> loginPageEvent.value = event
                "Register" -> registerPageEvent.value = event
            }
        }.addOnFailureListener {
            progressbarType.value = View.GONE
            loginToastText.value = "ログインに失敗しました"
        }
    }

    //------------------- common Login Function -------------------------------------------------

    private fun loginFunction(uid: String, fromFragment: String, userName: String = "") {
        var name = userName

        if (userName == "") {
            val ref = FirebaseDatabase.getInstance().getReference("users/${uid}")
            ref.get().addOnSuccessListener {
                name = it.getValue(User::class.java)?.userName!!
                loginToastText.value = "ようこそ ${name}さん"
            }
        } else {
                loginToastText.value = "ようこそ ${name}さん"
        }

        progressbarType.value = View.GONE
        if (fromFragment == "Register") {
            registerPageEvent.value = Event("enter")
        } else {
            loginPageEvent.value = Event("enter")
        }
    }


    //------------------ Email Signin --------------------------------

    // 選択された画像を表示
    fun imageSetFunction(data: Intent?, contentResolver: ContentResolver) {
        selectedPhotoUri = data?.data
        bitmap.value = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

        buttonAlpha.value = 0f

        userImage.value = true
    }

    // SignIn, Login処理開始
    fun performRegister(fragmentName: String) {

        progressbarType.value = View.VISIBLE
        var email = ""
        var pass = ""


        when (fragmentName) {
            "Register" -> {
                email = userEmail.value!!
                pass = userPassword.value!!
                if (signErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = signErrorMessage.joinToString(separator = "\n")
                    loginToastText.value = error
                    return
                }
            }
            "Login" -> {
                email = loginUserEmail.value!!
                pass = loginUserPass.value!!
                if (loginErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = loginErrorMessage.joinToString(separator = "\n")
                    loginToastText.value = error
                    return
                }
            }
        }


        when (fragmentName) {
            "Register" -> {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) return@addOnCompleteListener
                            uploadImageToFirebaseStorage(email)
                        }
                        .addOnFailureListener {
                            progressbarType.value = View.GONE
                            loginToastText.value = "ユーザー作成に失敗しました"
                        }
            }
            "Login" -> {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { data ->
                            loginFunction(data.user?.uid!!, fragmentName)
                        }
                        .addOnFailureListener {
                            progressbarType.value = View.GONE
                            when (it.message!!) {
                                myApplication.getString(R.string.login_error1),
                                myApplication.getString(R.string.login_error2) -> {
                                    loginToastText.value = "メールアドレスまたはパスワードが違います"
                                }
                                myApplication.getString(R.string.login_error3) -> {
                                    loginToastText.value = "規定回数ログインに失敗しましたので、時間をおいて再度ログインしてください。"
                                }

                            }
                        }
                }
        }
    }

    // 選択された画像をFirebaseStorageへ保存
    private fun uploadImageToFirebaseStorage(userEmail: String) {
        if (selectedPhotoUri == null) return

        // 一意の文字列
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString(), userName.value!!, userEmail, false, "Register")
                }
            }
            .addOnFailureListener { e ->
                progressbarType.value = View.GONE
            }
    }

    //-------------------- SNS Login -----------------------------------------

    private fun loginOrSignInCheck(userUid: String,
                                   fromFragment: String,
                                   profileImageUri: String,
                                   userName: String,
                                   userEmail: String,
    ) {
        val snsRef = FirebaseDatabase.getInstance().getReference("users/$userUid")
        snsRef.get()
            .addOnSuccessListener {
                if (it.getValue(User::class.java) != null) {
                    loginFunction(userUid, fromFragment)
                } else {
                    saveUserToFirebaseDatabase(profileImageUri, userName, userEmail, true, fromFragment)
                }
            }
            .addOnFailureListener {
                loginToastText.value = "failed : ${it.message}"
                progressbarType.value = View.GONE
            }
    }


    //------------------ Google Signin --------------------------------

    // googleからのSignIn開始
    fun googleSigninFunction(data: Intent?, fromFragment: String, activity: Activity) {
        progressbarType.value = View.VISIBLE
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, fromFragment,  activity)
        } catch (e: Exception) {
            loginToastText.value = "onActivityResult error: ${e.printStackTrace()}"
        }
    }

    // googleアカウントからユーザーデータ取得
    private fun firebaseAuthWithGoogle(idToken: String, fromFragment: String, activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid!!
                        val name = auth.currentUser?.displayName!!
                        val email = auth.currentUser?.email!!
                        val photoURL = auth.currentUser?.photoUrl.toString()
                        loginOrSignInCheck(uid, fromFragment, photoURL, name, email)
                    } else {
                        loginToastText.value = "firebaseAuthWithGoogle error: ${task.exception}"
                    }
                }
    }


}