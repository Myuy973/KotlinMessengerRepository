package com.example.kotlinmessenger.viewModel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.*
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
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.collections.HashMap

class UserPageViewModel : ViewModel() {

    val USER_KEY = "USER_KEY"
    val PROFILE_IMAGE_CHANGE = 2
    val IMAGE_SELECT = 1
    val IMAGE_SHOW = "IMAGE_SHOW"

    val LatestMessagesAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    private var LatestOldList = mutableListOf<LatestMessageRow>()
    private val latestMessageMap = HashMap<String, ChatMessage>()

    var updateProfileErrorList = mutableListOf<String>()
    lateinit var currentUserCopy: User
    lateinit var profileImageUri: Uri
    val editImageUri = MutableLiveData<String>("")
    val bitmap = MutableLiveData<Bitmap?>(null)
    val imageUpdateProcess = MutableLiveData<String>("")
    val editUserNameText = MutableLiveData<String>("")
    val editUserEmailText = MutableLiveData<String>("")
    val emailUpdateProcess = MutableLiveData<String>("")
    val editUserPassText = MutableLiveData<String>("")
    val passUpdateProcess = MutableLiveData<String>("")
    val updateButtonType = MutableLiveData<Boolean>(false)
    var updateAccessLimiter = false
    val rogressbarType = MutableLiveData<Int>(View.GONE)


    val NewMessageAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    var friendList = arrayOf<User>()

    val ChatLogAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    val chatInputText = MutableLiveData<String>("")
    var scrollPosition = MutableLiveData<Int>(0)
    private lateinit var childEventListener: ChildEventListener
    private var selectedImageUri: Uri? = null
    private var imageSelectedType = false

    var toUser: User? = User()


    companion object {
        lateinit var currentUser: User
    }


    // --------------------- common Function -------------------------------------------------

    fun printToast(text: String, activity: Activity) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }



    // --------------------- LatestMessage -------------------------------------------------

    fun verifyUserIsLoggedIn(activity: Activity) {
        // firebaseにログインしているかどうか
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
//            val intent = Intent(activity, RegisterActivity::class.java)
            val intent = Intent(activity, EntranceActivity::class.java)
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
//                            Log.d("log", "Login user: ${currentUser.userName}")
                        } catch (e: Exception) {
//                            Log.d("log", "login user error: $e")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
    }

    private fun refreshRecyclerViewMessages(addOrChangeType: String, changePosition: Int = 0, key: String = "") {
        if (addOrChangeType == "Add") {
            LatestMessagesAdapter.value?.clear()
            LatestOldList.clear()
            latestMessageMap.values.forEach {
                LatestOldList.add(LatestMessageRow(it, addOrChangeType))
            }
            try {
                LatestMessagesAdapter.value?.update(LatestOldList)
            } catch (e: java.lang.Exception) {
//                Log.d("log", "error: ${e.message}")
            }

        } else if (addOrChangeType == "Change") {
            LatestOldList[changePosition] = LatestMessageRow(latestMessageMap[key]!!, addOrChangeType)

            try {
                LatestMessagesAdapter.value?.update(LatestOldList)
            } catch (e: java.lang.Exception) {
//                Log.d("log", "error: ${e.message}")
            }
        }
    }


    fun listenForLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

//                Log.d("listenForLatestMessages", "database add changed!!! : ${chatMessage.text}")
//                Log.d("listenForLatestMessages", "snapshot changed!!! : ${snapshot}")

                latestMessageMap[snapshot.key!!] = chatMessage

//                Log.d("listenForLatestMessages", "latestMessageMap: ${latestMessageMap[snapshot.key]}, id: ${snapshot.key}")

                refreshRecyclerViewMessages("Add")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

//                Log.d("listenForLatestMessages", "database onChildChanged!!!: ${chatMessage.text}")

                latestMessageMap[snapshot.key!!] = chatMessage
                var latestChangePosition = 0
                var i = 0
                latestMessageMap.forEach { (key, _) ->
                    if (key == snapshot.key!!) latestChangePosition = i
                    i++
                }
                refreshRecyclerViewMessages("Change", latestChangePosition, snapshot.key!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    // ------------------------------ showProfileActivity ------------------------------------------------------------

    fun userInfoDisplay(userImage: String = currentUser.profileImageUri,
                        userName: String = currentUser.userName,
                        userEmail: String = currentUser.userEmail) {
        editImageUri.value = userImage
        editUserNameText.value = userName
        editUserEmailText.value = userEmail
        editUserPassText.value = ""
//        Log.d("log", "currentuser: ${currentUser.userName}, ${currentUser.userEmail}")
//        Log.d("log", "edittext: ${editUserNameText.value}, ${editUserEmailText.value}")
    }

    fun setUpchecker() {
        listOf(editUserNameText, editUserEmailText, editUserPassText).forEach { liveData ->
            liveData.asFlow()
                    .onEach { inputTextCheck() }
                    .launchIn(viewModelScope)
        }
    }

    fun profileImageChange(data: Intent?, activity: Activity) {
        profileImageUri = data?.data!!
        bitmap.value = MediaStore.Images.Media.getBitmap(activity.contentResolver, profileImageUri)
    }

    fun inputTextCheck() {
        updateButtonType.value =
                editUserEmailText.value?.isNotEmpty()!! &&
                editUserEmailText.value?.isNotEmpty()!! &&
                editUserPassText.value?.isNotEmpty()!!
    }

    fun userProfileUpdate(activity: Activity, loginType: Boolean) {

        if (loginType) {
            printToast("SNSログインの場合は編集できません", activity)
            return
        }

        updateAccessLimiter = false
        rogressbarType.value = View.VISIBLE

//        Log.d("log", "input Text: ${editUserNameText.value}, ${editUserEmailText.value}, ${editUserPassText.value}")
        updateProfileErrorList = mutableListOf()

        // 入力されたデータのチェック
        if (editUserNameText.value?.isEmpty()!!) {
            updateProfileErrorList.add("userNameを入力してください")
        }
        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (!emailPattern.matches(editUserEmailText.value.toString())) {
            updateProfileErrorList.add("正しいメールアドレスを入力してください")
        }
        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (!passPattern.matches(editUserPassText.value.toString())) {
            updateProfileErrorList.add("パスワードが英数字8文字以上ではありません")
        }

        if (updateProfileErrorList.isNotEmpty()) {
            val errorMessage = updateProfileErrorList.joinToString(separator = "\n")
            printToast(errorMessage, activity)
            rogressbarType.value = View.GONE
            return
        }

        // 更新処理

        val currentUserAuth = FirebaseAuth.getInstance().currentUser
        currentUserCopy = currentUser.copy()

//        Log.d("log", "currentUserCopy : ${currentUserCopy.userName}, ${currentUserCopy.userEmail}")
//        Log.d("log", "currentUser == currentUserCopy: ${currentUser == currentUserCopy}, currentUser === currentUserCopy : ${currentUser === currentUserCopy}")

        // user image
        if (bitmap.value != null) {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(profileImageUri)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            currentUserCopy.profileImageUri = it.toString()
                            editImageUri.value = it.toString()
                            imageUpdateProcess.value = "ok"
                        }
                    }
                    .addOnFailureListener {
                        imageUpdateProcess.value = "error"
                        updateProfileErrorList.add(errorSetter(it.message!!))
                    }
        } else { imageUpdateProcess.value = "ok"}

        // userName
        if (editUserNameText.value != currentUser.userName) {
//            Log.d("log", "name change")
            currentUserCopy.userName = editUserNameText.value!!
        }

        // userEmail
        if (editUserEmailText.value != currentUser.userEmail) {
//            Log.d("log", "email change: ${editUserEmailText.value}")
            currentUserAuth?.updateEmail(editUserEmailText.value!!)
                    ?.addOnSuccessListener {
//                        Log.d("log", "editUserEmailText value: ${editUserEmailText.value!!}")
//                        Log.d("log", "email before value: ${currentUserCopy.userEmail}")
                        currentUserCopy.userEmail = editUserEmailText.value!!
//                        Log.d("log", "email after value: ${currentUserCopy.userEmail}")
                        emailUpdateProcess.value = "ok"
                    }
                    ?.addOnFailureListener {
                        emailUpdateProcess.value = "error"
                        updateProfileErrorList.add(errorSetter(it.message!!))
//                        Log.d("log", "email error : ${it.message}")
                    }
        } else { emailUpdateProcess.value = "ok" }

        // userPass
        if (editUserPassText.value != "") {
//            Log.d("log", "pass change: ${editUserPassText.value}")
            currentUserAuth?.updatePassword(editUserPassText.value!!)
                    ?.addOnSuccessListener {
                        passUpdateProcess.value = "ok"
                    }
                    ?.addOnFailureListener {
//                        Log.d("log", "pass change failed: ${Thread.currentThread().name}")
                        passUpdateProcess.value = "error"
                        updateProfileErrorList.add(errorSetter(it.message!!))
//                        Log.d("log", "pass error : ${it.message}")
                    }
        } else { passUpdateProcess.value = "ok" }


    }

    // 更新処理
    suspend fun userdataUpdate(activity: Activity) {

//        Log.d("log", "userdataUpdate start")
//        Log.d("log", "imageUpdateProcess: ${imageUpdateProcess.value}, emailUpdateProcess: ${emailUpdateProcess.value}, passUpdateProcess: ${passUpdateProcess.value}, updateAccessLimiter: ${updateAccessLimiter},  ")
        if (imageUpdateProcess.value == "" || emailUpdateProcess.value == "" || passUpdateProcess.value == "" || updateAccessLimiter) {
//            Log.d("log", "userdataUpdate cancel")
            return
        }
//        Log.d("log", "userdataUpdate check ok")

        updateAccessLimiter = true

        val ref = FirebaseDatabase.getInstance().getReference("users")

        // currentUserCopyのデータに変化があれば更新
//        Log.d("log", "currentUserCopy : ${currentUserCopy.userName}, ${currentUserCopy.userEmail}")
////        Log.d("log", "currentUser == currentUserCopy: ${currentUser == currentUserCopy}, currentUser === currentUserCopy : ${currentUser === currentUserCopy}")
        if (currentUser != currentUserCopy) {
//            Log.d("log", "user change")
            ref.updateChildren(mapOf(currentUser.uid to currentUserCopy))
                    .addOnSuccessListener {
//                        Log.d("log", "user change success")
                    }
        }

//        Log.d("log", "errorlist ${updateProfileErrorList}")
        withContext(Dispatchers.Main) {
            if (updateProfileErrorList.isNotEmpty()) {
                rogressbarType.value = View.GONE
                if (updateProfileErrorList[0] == updateProfileErrorList[1]) {
                    printToast(updateProfileErrorList[0], activity)
                    return@withContext
                }
                val errorMessage = updateProfileErrorList.joinToString(separator = "\n")
                printToast(errorMessage, activity)
            } else {
                rogressbarType.value = View.GONE
                bitmap.value = null
                imageUpdateProcess.value = ""
                emailUpdateProcess.value = ""
                passUpdateProcess.value = ""
                updateAccessLimiter = false
                fetchCurrentUser()
                userInfoDisplay(currentUserCopy.profileImageUri, currentUserCopy.userName, currentUserCopy.userEmail)
                printToast("更新完了", activity)
            }
        }
    }

    // エラーメッセージからユーザへのメッセージを選別
    private fun errorSetter(error: String): String {
        return when (error) {
            "This operation is sensitive and requires recent authentication. Log in again before retrying this request." ->
                "再度ログインしてください"
            else -> "error"
        }
    }


    // ------------------------------ NewMessageActivity ------------------------------------------------

    fun fetchUserFriends(activity: Activity) {
        friendList = arrayOf()
        val ref = FirebaseDatabase.getInstance().getReference("/user-friends/${currentUser.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                NewMessageAdapter.value?.clear()
                snapshot.children.forEach {
//                    //                    Log.d("log", "current user: ${currentUser.uid}")
////                    Log.d("log", "user: ${it.getValue(User::class.java)?.userName}")
                    class FriendUserUid(val uid: String = "")
//                    val userUid = it.getValue(FriendUserUid::class.java)?.uid
//                    Log.d("log", "friend : ${it.value}")
                    val userUid = it.value as Map<*, *>
//                    Log.d("log", "friend : $userUid")
                    if (userUid["uid"] != currentUser.uid) {

                        val friendUserRef = FirebaseDatabase.getInstance().getReference("/users/${userUid["uid"]}")
                        friendUserRef.get().addOnSuccessListener { data ->
                            val friendUser = data.getValue(User::class.java)!!
                            friendList += friendUser
                            NewMessageAdapter.value?.add(UserItem(friendUser))
//                            Log.d("log", "add friendUser: ${friendUser.userName}")
//                            Log.d("log", "friendlist: ${friendList.map { it.userName }}")
                        }

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

    fun addFriendFunction(friendUid: String, activity: Activity) {
//        Log.d("log", "start addFriendFunction")

        if (currentUser.uid == friendUid) {
            printToast("自分を追加することはできません", activity)
            return
        } else if (friendUid.isEmpty()) {
            printToast("ユーザーのIDを入力してください", activity)
            return
        }

        val friendUserRef =
                FirebaseDatabase.getInstance().getReference("users/$friendUid")
        val myFriendRef =
                FirebaseDatabase.getInstance().getReference("user-friends/${currentUser.uid}/${friendUid}")
        val yourFriendRef =
                FirebaseDatabase.getInstance().getReference("user-friends/${friendUid}/${currentUser.uid}")

        friendUserRef.get().addOnSuccessListener {

            try {
                myFriendRef.setValue(mapOf("uid" to friendUid))
                yourFriendRef.setValue(mapOf("uid" to currentUser.uid))

                fetchUserFriends(activity)

                printToast("${it.getValue(User::class.java)?.userName}さんを追加しました！", activity)

            } catch (e: java.lang.Exception) {
                printToast("ユーザー追加に失敗しました", activity)
//                Log.d("log", "add friend error: ${e.printStackTrace()}")
            }

        }.addOnFailureListener {
                printToast("ユーザー追加に失敗しました", activity)
//                Log.d("log", "add friend error: ${it.printStackTrace()}")
        }


    }

    fun deleteFriendFunction(deleteList: List<Int>, activity: Activity) {

        if (deleteList.isEmpty()) {
            printToast("削除するユーザーを選んでください", activity)
            return
        }

        val deleteUserData = mutableListOf<User>()
        deleteList.forEach { num ->
            deleteUserData += friendList[num]
        }

        try {
            deleteUserData.forEach { deleteUser ->
                val removeMyFriendRef =
                        FirebaseDatabase.getInstance().getReference("user-friends/${currentUser.uid}/${deleteUser.uid}")
                val removeYourFriendRef =
                        FirebaseDatabase.getInstance().getReference("user-friends/${deleteUser.uid}/${currentUser.uid}")

                removeMyFriendRef.removeValue()
                removeYourFriendRef.removeValue()
            }
            fetchUserFriends(activity)
            printToast("削除に成功しました。", activity)
        } catch (e: java.lang.Exception) {
            printToast("削除に失敗しました。", activity)
        }

    }


    // ------------------------ ChatLogActivity ---------------------------------------------------


    fun listenForMessages(toUserData: User?, activity: Activity) {
        toUser = toUserData
        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance()
                .getReference("user-messages/$fromId/${toUser?.uid}")
        val latestRef =  FirebaseDatabase.getInstance()
                .getReference("latest-messages/$fromId")

        childEventListener = ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

//                Log.d("log", "add start")
                val chatMessage = snapshot.getValue(ChatMessage::class.java)


                // alreadyReadへの既読処理
                if (!chatMessage?.alreadyRead!!) {
//                    Log.d("log", snapshot.key!!)
                    chatMessage.alreadyRead = true
                }

//                Log.d("log", chatMessage.text)
                if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    val currentUser = currentUser
                    ChatLogAdapter.value?.add(
                        ChatFromItem(chatMessage.imageUrl, chatMessage.text, currentUser, activity)
                    )
                } else {
                    ChatLogAdapter.value?.add(
                        ChatToItem(chatMessage.imageUrl, chatMessage.text, toUser!!, activity)
                    )
                }

//                Log.d("log", "fromId: $fromId")
//                Log.d("log", "toUserId: ${toUser?.uid}")

                // 既読したメッセージをデータベースへ保存
                ref.updateChildren(mapOf(snapshot.key!! to chatMessage))
                latestRef.updateChildren(mapOf(toUser?.uid to chatMessage))

//                recyclerview_chat_log.scrollToPosition(ChatLogAdapter.value?.itemCount!! - 1)
                scrollPosition.value = ChatLogAdapter.value?.itemCount!! - 1

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // addChildEventListenerデタッチ処理
    fun eventlistenerFinish() {
        val fromId = currentUser.uid
        val ref = FirebaseDatabase.getInstance()
            .getReference("user-messages/$fromId/${toUser?.uid}")
        try {
            ref.removeEventListener(childEventListener)
//            Log.d("log", "finish success!!")
        } catch (e: java.lang.Exception) {
//            Log.d("log", "error: ${e.message}")
        }
    }


    fun performSendMessage(activity: Activity, imageUri: String = "") {

        val notEmptyPattern = Regex(""".+""")
        val omitToSpace = chatInputText.value!!.filter { !Regex("""\s""").matches(it.toString()) }
//        Log.d("log", "subText: [${omitToSpace}]")
//        Log.d("log", "${!notEmptyPattern.matches(omitToSpace)}, ${omitToSpace.isEmpty()}, ${!imageSelectedType}")
        if ((!notEmptyPattern.matches(omitToSpace) || omitToSpace.isEmpty()) && !imageSelectedType) {
            printToast("テキストを入力してください", activity)
            return
        }

        imageSelectedType = false
        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = toUser?.uid!!

        val reference = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId").push()
//        Log.d("log", "parameter set ok")

//        Log.d("log", "${reference.key!!}, ${chatInputText.value!!}, ${fromId}, ${toId}, ${System.currentTimeMillis() / 1000}")
        // クラスにして送らないと送信できない(Activityも落ちる)
        val chatMessage = ChatMessage(reference.key!!, chatInputText.value!!, imageUri, fromId, toId, System.currentTimeMillis() / 1000, false)
        reference.setValue(chatMessage)
                .addOnSuccessListener {
//                    Log.d("log", "Saved our chat message to From: ${it}")
//                    Log.d("log", "inputText: [${chatInputText.value}]")
//                    Log.d("log", "inputText length: [${chatInputText.value!!.length}]")
                    chatInputText.value = ""
////                Log.d("log", "inputText: ${chatInputText.value}")
//                    recyclerview_chat_log.scrollToPosition(ChatLogAdapter.value?.itemCount!! - 1)
                    try {
                        scrollPosition.value = ChatLogAdapter.value?.itemCount!! - 1
                    } catch (e: java.lang.Exception) {
//                        Log.d("log", "error: ${e.printStackTrace()}")
                    }
                }
        toReference.setValue(chatMessage)
                .addOnSuccessListener {
//                    Log.d("log", "Saved our chat message to To: ${it}")
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
        imageSelectedType = true
        selectedImageUri = data?.data

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/send_image/$filename")
        ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
//                        Log.d("log", "image uri: ${it}")
                        performSendMessage(activity, it.toString())
                    }
                }
    }

    @SuppressWarnings("unchecked")
    fun changeToShowActivity(imageView: View, imageUri: String, activity: Activity) {

        imageView.visibility = View.VISIBLE

        val intent = Intent(activity, ShowActivity::class.java)
//        Log.d("log", "imageuri: $imageUri")
        intent.putExtra(IMAGE_SHOW, imageUri)

        val activityOptions: ActivityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                Pair(imageView, ShowActivity().VIEW_NAME_HEADER_IMAGE)
        )

        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle())

    }





}