package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.app.Application
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
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class LoginViewModel(
        private val myApplication: Application,
) : AndroidViewModel(myApplication) {

    val IMAGE_INPUT = 1
    val GOOGLE_SIGNIN = 2


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
    private var auth: FirebaseAuth

    var requestCode: Int = 0
    lateinit var startActivityForResultIntent: Intent

    var buttonAlpha = MutableLiveData<Float>(1f)
    private var selectedPhotoUri: Uri? = null
    var bitmap = MutableLiveData<Bitmap?>(null)

    private val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("twitter.com")

    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("693890654310-4h5vpi1psul17adjt04cmqdsit9tpb8g.apps.googleusercontent.com")
            .requestEmail()
            .build()
    //-------------------------------------------------------

    companion object {
        // entrance page event
        val entrancePageEvent = MutableLiveData<Event<String>>()
        // entrance toast
        val entranceToastText = MutableLiveData<String>("")
    }



    init {

//        Log.d("log", "start Login view model")

        auth = Firebase.auth
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
//        Log.d("log", "name: ${userName.value}, email: ${userEmail.value}, pass: ${userPassword.value}")
//        Log.d("log", "signinButtonType: ${signinButtonType.value}")

    }

    private fun checkLoginItem() {

        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (emailPattern.matches(loginUserEmail.value.toString())) {
            loginErrorMessage.remove(myApplication.getString(R.string.email_input_error))
        } else if (!loginErrorMessage.contains(myApplication.getString(R.string.email_input_error))) {
            loginErrorMessage.add(myApplication.getString(R.string.email_input_error)!!)
        }


        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (passPattern.matches(loginUserPass.value.toString())) {
            loginErrorMessage.remove(myApplication.getString(R.string.pass_mismatch))
        } else if (!loginErrorMessage.contains(myApplication.getString(R.string.pass_mismatch))) {
            loginErrorMessage.add(myApplication.getString(R.string.pass_mismatch))
        }

//        Log.d("log", "$loginErrorMessage")


        loginButtonType.value =
                !loginUserEmail.value?.isEmpty()!! &&
                !loginUserPass.value?.isEmpty()!!

    }


    // ----------------- common Function -------------------------------------------
//    fun toastPrint(text: String, activityName: String) {
//        when (activityName) {
//            ".view.RegisterActivity" -> {
//                registerToastText.value = text
//            }
//            ".view.LoginActivity" -> {
//                loginToastText.value = text
//            }
//        }
//    }

    // ----------------- common Signin Function -------------------------------------------

    private fun saveUserToFirebaseDatabase(profileImageUri: String,
                                           userName: String,
                                           userEmail: String,
                                           snsLogin: Boolean) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = Firebase.database.getReference("users/$uid")

        val user = User(uid, userName, userEmail,  profileImageUri)

        ref.setValue(user).addOnSuccessListener {
//            toastPrint("ようこそ ${user.userName}さん", activityName)
            entranceToastText.value = "ようこそ ${user.userName}さん"
            if (snsLogin) {
                entrancePageEvent.value = Event("enterWithSNS")
            } else {
                entrancePageEvent.value = Event("enter")
            }
//            Log.d("log", "Finally we saved the user to Firebase Database")
//            Log.d("log", "snsLogin: $snsLogin")
//            val intent = Intent(activity, LatestMessagesActivity::class.java)
//            intent.putExtra("fromActivity", "SigninOrLogin")
//            intent.putExtra("snsLogin", snsLogin)
//            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            activity.startActivity(intent)
        }.addOnFailureListener {
            progressbarType.value = View.GONE
//            Log.d("log", "save is not Success")
        }
    }

    //------------------ Email Signin --------------------------------

    fun imageSetFunction(data: Intent?, activity: Activity) {
        selectedPhotoUri = data?.data
//        Log.d("log", "bitmap: ${bitmap.value ?: "null"}")
        bitmap.value = MediaStore.Images.Media.getBitmap(activity.contentResolver, selectedPhotoUri)

        buttonAlpha.value = 0f

        userImage.value = true
    }



    fun performRegister(fragmentName: String) {

        progressbarType.value = View.VISIBLE
        var email = ""
        var pass = ""

        Log.d("log", "performRegister start: $fragmentName")

        when (fragmentName) {
            "Register" -> {
                email = userEmail.value!!
                pass = userPassword.value!!
                if (signErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = signErrorMessage.joinToString(separator = "\n")
//                    toastPrint(error, ".view.RegisterActivity")
                    entranceToastText.value = error
//                    Log.d("log", error)
                    return
                }
            }
            "Login" -> {
                email = loginUserEmail.value!!
                pass = loginUserPass.value!!
                if (loginErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = loginErrorMessage.joinToString(separator = "\n")
//                    toastPrint(error, ".view.LoginActivity")
                    entranceToastText.value = error
//                    Log.d("log", error)
                    return
                }
            }
        }


        when (fragmentName) {
            "Register" -> {
//                Log.d("log", "register activity")
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) return@addOnCompleteListener
//                            toastPrint("Login Successfully", activity)
                            uploadImageToFirebaseStorage(email)
                        }
                        .addOnFailureListener {
                            progressbarType.value = View.GONE
//                            Log.d("log", "error: ${it.message}")
//                            toastPrint("ユーザー作成に失敗しました。", activity)
                            entranceToastText.value = "ユーザー作成に失敗しました"
                        }
            }
            "Login" -> {
//                Log.d("log", "loginActivity")
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { data ->

                            val ref = FirebaseDatabase.getInstance().getReference("users/${data.user?.uid}")
                            ref.get().addOnSuccessListener {
                                val user = it.getValue(User::class.java)
//                                toastPrint("ようこそ ${user?.userName}", activity)
                                entranceToastText.value = "ようこそ ${user?.userName}"
                            }

                            entrancePageEvent.value = Event("enter")
                        //                            val intent = Intent(activity, LatestMessagesActivity::class.java)
//                            intent.putExtra("fromActivity", "SigninOrLogin")
//                            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
//                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                            activity.startActivity(intent)

//                            Log.d("log", "Successfully Login")
                        }
                        .addOnFailureListener {
//                            Log.d("log", "Failed to login: ${it.message}")
                            progressbarType.value = View.GONE
                            when (it.message!!) {
                                myApplication.getString(R.string.login_error1),
                                myApplication.getString(R.string.login_error2) -> {
//                                    toastPrint("メールアドレスまたはパスワードが違います。", activity)
                                    entranceToastText.value = "メールアドレスまたはパスワードが違います"
                                }
                                myApplication.getString(R.string.login_error3) -> {
//                                    toastPrint("規定回数ログインに失敗しましたので、時間をおいて再度ログインしてください。", activity)
                                    entranceToastText.value = "規定回数ログインに失敗しましたので、時間をおいて再度ログインしてください。"
                                }

                            }
                        }
                }
        }
    }

    private fun uploadImageToFirebaseStorage(userEmail: String) {
        if (selectedPhotoUri == null) return

//        Log.d("log", "uploadImageToFirebaseStorage start")
        // 一意の文字列
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
//                Log.d("log", "successfully uploaded image")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString(), userName.value!!, userEmail, false)
                }
            }
            .addOnFailureListener { e ->
                progressbarType.value = View.GONE
//                Log.d("log", "error: ${e.printStackTrace()}")
            }
    }





    //------------------ Google Signin --------------------------------


    fun googleSigninFunction(data: Intent?, activity: Activity) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, activity)
        } catch (e: Exception) {
            entranceToastText.value = "onActivityResult error: ${e.printStackTrace()}"
        //            toastPrint("onActivityResult error: ${e.printStackTrace()}", activity)
//            Log.d("log", "onActivityResult error: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, activity: Activity) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val name = auth.currentUser?.displayName
                        val email = auth.currentUser?.email
                        val photourl = auth.currentUser?.photoUrl
//                        Log.d("log", "google signin success : name: $name, email: $email, photourl: $photourl")
                        saveUserToFirebaseDatabase(photourl!!.toString(), name!!, email!!, true)
                    } else {
                        entranceToastText.value = "firebaseAuthWithGoogle error: ${task.exception}"
//                        toastPrint("firebaseAuthWithGoogle error: ${task.exception}", activity)
//                        Log.d("log", "firebaseAuthWithGoogle error: ${task.exception}")
                    }
                }
    }


}