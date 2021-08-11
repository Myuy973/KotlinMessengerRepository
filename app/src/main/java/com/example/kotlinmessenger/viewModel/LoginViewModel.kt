package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.view.LatestMessagesActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class LoginViewModel: ViewModel() {

    val IMAGE_INPUT = 1
    val GOOGLE_SIGNIN = 2

    val userImage = MutableLiveData<Boolean>(false)
    val userName = MutableLiveData<String>("")
    val userEmail = MutableLiveData<String>("")
    val userPassword = MutableLiveData<String>("")
    val signinButtonType = MutableLiveData<Boolean>(false)

    val loginUserEmail = MutableLiveData<String>("")
    val loginUserPass = MutableLiveData<String>("")
    val loginButtonType = MutableLiveData<Boolean>(false)

    lateinit var googleSignInClient: GoogleSignInClient
    private var auth: FirebaseAuth

    var requestCode: Int = 0
    lateinit var startActivityForResultIntent: Intent

    var buttonAlpha = MutableLiveData<Float>(1f)
    private var selectedPhotoUri: Uri? = null
    var bitmap = MutableLiveData<Bitmap?>(null)

    val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("693890654310-4h5vpi1psul17adjt04cmqdsit9tpb8g.apps.googleusercontent.com")
            .requestEmail()
            .build()


    init {

        Log.d("value", "start Loginviewmodel")

        auth = Firebase.auth

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

        Log.d("value", "start checkTextEmpty")
//        Log.d("value", "image: ${userImage.value}, name: ${!userName.value?.isEmpty()!!}, email: ${!userEmail.value?.isEmpty()!!}, pass: ${!userPassword.value?.isEmpty()!!}")
//        Log.d("value", "image: ${userImage.value}, name: ${userName.value}, email: ${userEmail.value}, pass: ${userPassword.value}")
        signinButtonType.value =
                userImage.value!! &&
                !userName.value?.isEmpty()!! &&
                !userEmail.value?.isEmpty()!! &&
                !userPassword.value?.isEmpty()!!
        Log.d("value", "signinButtonType: ${signinButtonType.value}")

    }

    private fun checkLoginItem() {

        loginButtonType.value =
                !loginUserEmail.value?.isEmpty()!! &&
                !loginUserPass.value?.isEmpty()!!

    }


    // ----------------- common Function -------------------------------------------
    fun toastPrint(text: String, activity: Activity) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    // ----------------- common Signin Function -------------------------------------------

    fun saveUserToFirebaseDatabase(activity: Activity, profileImageUri: String, username: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = Firebase.database.getReference("users/$uid")

        val user = User(uid, username, profileImageUri)

        ref.setValue(user).addOnSuccessListener {
            Log.d("value", "Finally we saved the user to Firebase Database")
            val intent = Intent(activity, LatestMessagesActivity::class.java)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }.addOnFailureListener {
            Log.d("value", "save is not Success")
        }
    }

    //------------------ Email Signin --------------------------------

    fun imageSetFunction(data: Intent?, activity: Activity) {
        selectedPhotoUri = data?.data
        Log.d("value", "bitmap: ${bitmap.value ?: "null"}")
        bitmap.value = MediaStore.Images.Media.getBitmap(activity.contentResolver, selectedPhotoUri)

        buttonAlpha.value = 0f

        userImage.value = true

    }

    fun inputImage(activity: Activity) {
//        Log.d("value", "activity: ${activity.componentName.shortClassName.matches(Regex(""".*RegisterActivity"""))}")
        startActivityForResultIntent = Intent(Intent.ACTION_PICK)
        startActivityForResultIntent.type = "image/*"
        requestCode = IMAGE_INPUT
        activity.startActivityForResult(startActivityForResultIntent, requestCode)
    }


    fun performRegister(activity: Activity) {

        Log.d("value", "registerbutton push")

        val shortActivityName = activity.componentName.shortClassName
        var email = ""
        var pass = ""

        when (shortActivityName) {
            ".view.RegisterActivity" -> {
                email = userEmail.value!!
                pass = userPassword.value!!
            }
            ".view.LoginActivity" -> {
                email = loginUserEmail.value!!
                pass = loginUserPass.value!!
            }
        }

        if (email.isEmpty() || pass.isEmpty()) {
            toastPrint("Please enter text in text/ps", activity)
            return
        }

        when (shortActivityName) {
            ".view.RegisterActivity" -> {
                Log.d("value",  "register activity")
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener
                        toastPrint("Login Successfully", activity)
                        uploadImageToFirebaseStorage(activity)
                    }
                .addOnFailureListener {
                    toastPrint("Failed to create user: ${it.message}", activity)
                }
            }
            ".view.LoginActivity" -> {
                Log.d("value",  "loginActivity")
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        val intent = Intent(activity, LatestMessagesActivity::class.java)
                        // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        activity.startActivity(intent)

                        Log.d("value",  "Successfully Login")
                        toastPrint( "Successfully Login", activity)
                    }
                    .addOnFailureListener {
                        Log.d("value",  "Failed to login: ${it.printStackTrace()}")
                        when (it.message!!) {
                            R.string.login_email_or_pass_error.toString() -> {
                                toastPrint("メールアドレスまたはパスワードが違います。", activity)
                            }
                            R.string.login_failed_several_times.toString() -> {
                                toastPrint("規定回数ログインに失敗しましたので、時間をおいて再度ログインしてください。", activity)
                            }

                        }
                    }
            }
        }



    }

    private fun uploadImageToFirebaseStorage(activity: Activity) {
        if (selectedPhotoUri == null) return

        Log.d("value", "uploadImageToFirebaseStorage start")
        // 一意の文字列
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("value", "successfully uploaded image")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(activity, it.toString(), userName.value!!)
                }
            }
            .addOnFailureListener { e ->
                Log.d("value", "error: ${e.printStackTrace()}")
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
            Log.d("value", "onActivityResult error: ${e.message}")
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
                        Log.d("value", "google signin success : name: $name, email: $email, photourl: $photourl")
                        saveUserToFirebaseDatabase(activity, photourl!!.toString(), name!!)
                    } else {
                        toastPrint("firebaseAuthWithGoogle error: ${task.exception}", activity)
                        Log.d("value", "firebaseAuthWithGoogle error: ${task.exception}")
                    }
                }
    }

}