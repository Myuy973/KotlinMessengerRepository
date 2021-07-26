package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlinmessenger.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedPhotoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register)


        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_an_account_textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        selectphoto_button_register.setOnClickListener {
            Log.d("value", "try  to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("value", "Photo was selected")
            
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            binding.selectphotoButtonRegister.setBackgroundDrawable(bitmapDrawable)

            
        }

    }

    private fun performRegister() {

        Log.d("value", "registerbutton push")

        val email = email_edittext_register.text.toString()
        val pass = password_edittext_register.text.toString()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter text in text/ps", Toast.LENGTH_SHORT)
                .show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT)
                    .show()
                uploadImageToFirebaseStorage()

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }


    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        Log.d("value", "uploadImageToFirebaseStorage start")
        // 一意の文字列
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("value", "successfully uploaded image")
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.d("value", "error: ${e.printStackTrace()}")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUri: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
//        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")
        val ref = Firebase.database.getReference("users/$uid")
        val username = username_edittext_register.text.toString()

        val user = User(uid, username, profileImageUri)

        Log.d("value", "save start")

        ref.setValue(user).addOnSuccessListener {
            Log.d("value", "Finally we saved the user to Firebase Database")
            val intent = Intent(this, LatestMessagesActivity::class.java)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }.addOnFailureListener {
            Log.d("value", "save is not Success")
        }

//        Log.d("value", "save is: ${result.exception?.printStackTrace()}")

        Log.d("value", "save end")
    }


}