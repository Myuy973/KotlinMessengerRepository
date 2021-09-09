package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.view.LatestMessagesActivity
import com.example.kotlinmessenger.view.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.IllegalStateException
import java.util.*

class LoginViewModel: ViewModel() {

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

    private val errorList = mapOf(
            "nameEmpty" to "UserNameを入力してください",
            "emailMissMatch" to "正しいメールアドレスと入力してください",
            "passMissMatch" to "パスワードが英数字8文字以上ではありません",
    )

    private val errorMessageList = mapOf(
            "passMissMatch" to "The password is invalid or the user does not have a password.",
            "userNotFound" to "There is no user record corresponding to this identifier. The user may have been deleted.",
            "passTooWrong" to "We have blocked all requests from this device due to unusual activity. Try again later. [ Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later. ]",
    )




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


    init {

        Log.d("log", "start Login view model")

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
            signErrorMessage.remove(errorList["nameEmpty"])
        } else if (!signErrorMessage.contains(errorList["nameEmpty"])) {
            signErrorMessage.add(errorList["nameEmpty"]!!)
        }

        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (emailPattern.matches(userEmail.value.toString())) {
            signErrorMessage.remove(errorList["emailMissMatch"])
        } else if (!signErrorMessage.contains(errorList["emailMissMatch"])) {
            signErrorMessage.add(errorList["emailMissMatch"]!!)
        }


        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (passPattern.matches(userPassword.value.toString())) {
            signErrorMessage.remove(errorList["passMissMatch"])
        } else if (!signErrorMessage.contains(errorList["passMissMatch"])) {
            signErrorMessage.add(errorList["passMissMatch"]!!)
        }

        Log.d("log", "$signErrorMessage")


        Log.d("log", "start checkTextEmpty")
        signinButtonType.value =
                userImage.value!! &&
                !userName.value?.isEmpty()!! &&
                !userEmail.value?.isEmpty()!! &&
                !userPassword.value?.isEmpty()!!
        Log.d("log", "name: ${userName.value}, email: ${userEmail.value}, pass: ${userPassword.value}")
        Log.d("log", "signinButtonType: ${signinButtonType.value}")

    }

    private fun checkLoginItem() {

        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (emailPattern.matches(loginUserEmail.value.toString())) {
            loginErrorMessage.remove(errorList["emailMissMatch"])
        } else if (!loginErrorMessage.contains(errorList["emailMissMatch"])) {
            loginErrorMessage.add(errorList["emailMissMatch"]!!)
        }


        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (passPattern.matches(loginUserPass.value.toString())) {
            loginErrorMessage.remove(errorList["passMissMatch"])
        } else if (!loginErrorMessage.contains(errorList["passMissMatch"])) {
            loginErrorMessage.add(errorList["passMissMatch"]!!)
        }

        Log.d("log", "$loginErrorMessage")


        loginButtonType.value =
                !loginUserEmail.value?.isEmpty()!! &&
                !loginUserPass.value?.isEmpty()!!

    }


    // ----------------- common Function -------------------------------------------
    fun toastPrint(text: String, activity: Activity) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    // ----------------- common Signin Function -------------------------------------------

    private fun saveUserToFirebaseDatabase(activity: Activity,
                                           profileImageUri: String,
                                           userName: String,
                                           userEmail: String,
                                           snsLogin: Boolean) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = Firebase.database.getReference("users/$uid")

        val user = User(uid, userName, userEmail,  profileImageUri)

        ref.setValue(user).addOnSuccessListener {
            toastPrint("ようこそ ${user.userName}さん", activity)
//            Log.d("log", "Finally we saved the user to Firebase Database")
            Log.d("log", "snsLogin: $snsLogin")
            val intent = Intent(activity, LatestMessagesActivity::class.java)
            intent.putExtra("fromActivity", "SigninOrLogin")
            intent.putExtra("snsLogin", snsLogin)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }.addOnFailureListener {
            progressbarType.value = View.GONE
            Log.d("log", "save is not Success")
        }
    }

    //------------------ Email Signin --------------------------------

    fun imageSetFunction(data: Intent?, activity: Activity) {
        selectedPhotoUri = data?.data
        Log.d("log", "bitmap: ${bitmap.value ?: "null"}")
        bitmap.value = MediaStore.Images.Media.getBitmap(activity.contentResolver, selectedPhotoUri)

        buttonAlpha.value = 0f

        userImage.value = true

    }

    fun inputImage(activity: Activity) {
        startActivityForResultIntent = Intent(Intent.ACTION_PICK)
        startActivityForResultIntent.type = "image/*"
        requestCode = IMAGE_INPUT
        activity.startActivityForResult(startActivityForResultIntent, requestCode)
    }


    fun performRegister(activity: Activity) {

        Log.d("log", "registerbutton push")

        progressbarType.value = View.VISIBLE
        val shortActivityName = activity.componentName.shortClassName
        var email = ""
        var pass = ""


        when (shortActivityName) {
            ".view.RegisterActivity" -> {
                email = userEmail.value!!
                pass = userPassword.value!!
                if (signErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = signErrorMessage.joinToString(separator = "\n")
                    toastPrint(error, activity)
                    Log.d("log", error)
                    return
                }
            }
            ".view.LoginActivity" -> {
                email = loginUserEmail.value!!
                pass = loginUserPass.value!!
                if (loginErrorMessage.isNotEmpty()) {
                    progressbarType.value = View.GONE
                    val error = loginErrorMessage.joinToString(separator = "\n")
                    toastPrint(error, activity)
                    Log.d("log", error)
                    return
                }
            }
        }


        when (shortActivityName) {
            ".view.RegisterActivity" -> {
                Log.d("log", "register activity")
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) return@addOnCompleteListener
//                            toastPrint("Login Successfully", activity)
                            uploadImageToFirebaseStorage(activity, email)
                        }
                        .addOnFailureListener {
                            progressbarType.value = View.GONE
                            Log.d("log", "error: ${it.message}")
                            toastPrint("ユーザー作成に失敗しました。", activity)
                        }
            }
            ".view.LoginActivity" -> {
                Log.d("log", "loginActivity")
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { data ->

                            val ref = FirebaseDatabase.getInstance().getReference("users/${data.user?.uid}")
                            ref.get().addOnSuccessListener {
                                val user = it.getValue(User::class.java)
                                toastPrint("ようこそ ${user?.userName}", activity)
                            }

                            val intent = Intent(activity, LatestMessagesActivity::class.java)
                            intent.putExtra("fromActivity", "SigninOrLogin")
                            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            activity.startActivity(intent)

                            Log.d("log", "Successfully Login")
                        }
                        .addOnFailureListener {
                            Log.d("log", "Failed to login: ${it.message}")
                            progressbarType.value = View.GONE
                            when (it.message!!) {
                                errorMessageList["passMissMatch"],
                                errorMessageList["userNotFound"] -> {
                                    toastPrint("メールアドレスまたはパスワードが違います。", activity)
                                }
                                errorMessageList["passTooWrong"] -> {
                                    toastPrint("規定回数ログインに失敗しましたので、時間をおいて再度ログインしてください。", activity)
                                }

                            }
                        }
                }
        }
    }

    private fun uploadImageToFirebaseStorage(activity: Activity, userEmail: String) {
        if (selectedPhotoUri == null) return

        Log.d("log", "uploadImageToFirebaseStorage start")
        // 一意の文字列
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("log", "successfully uploaded image")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(activity, it.toString(), userName.value!!, userEmail, false)
                }
            }
            .addOnFailureListener { e ->
                progressbarType.value = View.GONE
                Log.d("log", "error: ${e.printStackTrace()}")
            }
    }





    //------------------ Google Signin --------------------------------

    fun googleSignin(activity: Activity) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResultIntent = signInIntent
        requestCode = GOOGLE_SIGNIN
        activity.startActivityForResult(startActivityForResultIntent, requestCode)
        //        startActivityForResultFunction = googleSigninFunction
    }

    fun googleSigninFunction(data: Intent?, activity: Activity) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, activity)
        } catch (e: Exception) {
            toastPrint("onActivityResult error: ${e.printStackTrace()}", activity)
            Log.d("log", "onActivityResult error: ${e.message}")
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
                        Log.d("log", "google signin success : name: $name, email: $email, photourl: $photourl")
                        saveUserToFirebaseDatabase(activity, photourl!!.toString(), name!!, email!!, true)
                    } else {
                        toastPrint("firebaseAuthWithGoogle error: ${task.exception}", activity)
                        Log.d("log", "firebaseAuthWithGoogle error: ${task.exception}")
                    }
                }
    }


}