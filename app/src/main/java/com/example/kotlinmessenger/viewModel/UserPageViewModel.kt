package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.util.Pair
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlinmessenger.model.ChatItem.ChatFromItem
import com.example.kotlinmessenger.model.ChatItem.ChatToItem
import com.example.kotlinmessenger.model.ChatMessage
import com.example.kotlinmessenger.model.LatestMessageRow
import com.example.kotlinmessenger.model.User
import com.example.kotlinmessenger.model.UserItem
import com.example.kotlinmessenger.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupieAdapter
import java.util.*
import kotlin.collections.HashMap

class UserPageViewModel: ViewModel() {

    val USER_KEY = "USER_KEY"
    val IMAGE_SELECT = 1
    val IMAGE_SHOW = "IMAGE_SHOW"

    val LatestMessagesAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    val NewMessageAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    val ChatLogAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    val chatInputText = MutableLiveData<String>("")
    var scrollPosition = MutableLiveData<Int>(0)

    private var selectedImageUri: Uri? = null
    private val latestMessageMap = HashMap<String, ChatMessage>()
    var toUser: User? = User()

    companion object {
        lateinit var currentUser: User
    }



    // --------------------- common Function -------------------------------------------------

    private fun printToast(text: String, activity: Activity) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }



    // --------------------- LatestMessage -------------------------------------------------

    fun verifyUserIsLoggedIn(activity: Activity) {
        // firebaseにログインしているかどうか
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(activity, RegisterActivity::class.java)
            // activityのバックスタックを消し、新しくバックスタックを作り直す（戻るを押すとアプリが落ちる）
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

     fun fetchCurrentUser() {
        val uid = Firebase.auth.currentUser?.uid
        FirebaseDatabase.getInstance().getReference("/users/$uid")
                .addListenerForSingleValueEvent( object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            currentUser = snapshot.getValue(User::class.java)!!
                            Log.d("value", "Login user: ${currentUser.username}")
                        } catch (e: Exception) {
                            Log.d("value", "login user error: $e")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun refreshRecyclerViewMessages() {
        LatestMessagesAdapter.value?.clear()
        latestMessageMap.values.forEach {
            LatestMessagesAdapter.value?.add(LatestMessageRow(it))
        }
    }

    fun listenForLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                Log.d("listenForLatestMessages", "database add changed!!! : ${chatMessage.text}")
                Log.d("listenForLatestMessages", "snapshot changed!!! : ${snapshot}")

                latestMessageMap[snapshot.key!!] = chatMessage

                Log.d("listenForLatestMessages", "latestMessageMap: ${latestMessageMap[snapshot.key]}, id: ${snapshot.key}")

                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                Log.d("listenForLatestMessages", "database onChildChanged!!!: ${chatMessage.text}")

                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    // ------------------------------ NewMessageActivity ------------------------------------------------

    fun fetchUsers(activity: Activity) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                NewMessageAdapter.value?.clear()
                snapshot.children.forEach {
                    Log.d("value", "currentuser: ${currentUser.uid}")
                    Log.d("value", "user: ${it.getValue(User::class.java)?.username}")
                    val user = it.getValue(User::class.java)
                    Log.d("value", "${user?.uid != currentUser.uid}, ${user?.uid}, ${currentUser.uid}")
                    if (user != null && user.uid != currentUser.uid) {
                        Log.d("value", "UserItem add to adapter")
                        NewMessageAdapter.value?.add(UserItem(user))
                    }
                }

                NewMessageAdapter.value?.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    activity.startActivity(intent)

                    activity.finish()
                }


            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    // ------------------------ ChatLogActivity ---------------------------------------------------


    fun listenForMessages(toUserData: User?, activity: Activity) {
        toUser = toUserData
        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance()
                .getReference("user-messages/$fromId/${toUser?.uid}")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                Log.d("value", "add start")
                Log.d("value", "adapter: ${ChatLogAdapter.value?.itemCount}")
                Log.d("value", "snapshot: ${snapshot.getValue()}")
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d("value", chatMessage.text)
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = currentUser
                        ChatLogAdapter.value?.add(ChatFromItem(chatMessage.imageUrl, chatMessage.text, currentUser, activity))
                    } else {
                        ChatLogAdapter.value?.add(ChatToItem(chatMessage.imageUrl, chatMessage.text, toUser!!, activity))
                    }
                }

//                recyclerview_chat_log.scrollToPosition(ChatLogAdapter.value?.itemCount!! - 1)
                scrollPosition.value = ChatLogAdapter.value?.itemCount!! - 1

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    fun performSendMessage(activity: Activity, imageUri: String = "") {

//        if (chatInputText.value!! == "") {
//            printToast("テキストを入力してください", activity)
//            return
//        }

        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = toUser?.uid!!

        val reference = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId").push()
        Log.d("value", "parameter set ok")

        Log.d("value", "${reference.key!!}, ${chatInputText.value!!}, ${fromId}, ${toId}, ${System.currentTimeMillis() / 1000}")
        // クラスにして送らないと送信できない(Activityも落ちる)
        val chatMessage = ChatMessage(reference.key!!, chatInputText.value!!, imageUri, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
                .addOnSuccessListener {
                Log.d("value", "Saved our chat message to From: ${it}")
                Log.d("value", "inputText: ${chatInputText.value}")
                    chatInputText.value = ""
                Log.d("value", "inputText: ${chatInputText.value}")
//                    recyclerview_chat_log.scrollToPosition(ChatLogAdapter.value?.itemCount!! - 1)
                    try {
                        scrollPosition.value = ChatLogAdapter.value?.itemCount!! - 1
                    } catch (e: java.lang.Exception) {
                        Log.d("value", "error: ${e.printStackTrace()}")
                    }
                }
        toReference.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("value", "Saved our chat message to To: ${it}")
                }


        val latestMessageRef = FirebaseDatabase.getInstance()
                .getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
                .getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }

    fun imageSelecterStart(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activity.startActivityForResult(intent, IMAGE_SELECT)
    }

    fun imageSelectedFunction(data: Intent?, activity: Activity) {
        selectedImageUri = data?.data

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/send_image/$filename")
        ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("value", "image uri: ${it.toString()}")
                        performSendMessage(activity, it.toString())
                    }
                }


    }

    @SuppressWarnings("unchecked")
    fun changeToShowActivity(imageView: View, imageUri: String, activity: Activity) {
        val intent = Intent(activity, ShowActivity::class.java)
        Log.d("value", "imageuri: $imageUri")
        intent.putExtra(IMAGE_SHOW, imageUri)

        val activityOptions: ActivityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                Pair(imageView, ShowActivity().VIEW_NAME_HEADER_IMAGE)
        )

        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())

    }





}