package com.simple.friendship_messenger.viewModel

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.core.util.Pair
import androidx.lifecycle.*
import com.simple.friendship_messenger.R
import com.simple.friendship_messenger.model.*
import com.simple.friendship_messenger.model.chatItem.ChatFromItem
import com.simple.friendship_messenger.model.chatItem.ChatToItem
import com.simple.friendship_messenger.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupieAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class UserPageViewModel(
    private val myApplication: Application
) : AndroidViewModel(myApplication) {

    val PROFILE_IMAGE_CHANGE = 2
    val IMAGE_SELECT = 1
    val IMAGE_SHOW = "IMAGE_SHOW"

    val latestLoadingValue = MutableLiveData<Int>(View.VISIBLE)
    val latestMessagePageEvent = MutableLiveData<Event<String>>()
    val latestMessagesAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    private var latestOldList = mutableListOf<LatestMessageRow>()
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
    val passEditTextEnableType = MutableLiveData<Boolean>(true)
    val passEditTextHint = MutableLiveData<String>("")
    val updateButtonType = MutableLiveData<Boolean>(false)
    var updateAccessLimiter = false
    val progressBarType = MutableLiveData<Int>(View.GONE)


    val newMessageAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    var friendList = arrayOf<User>()

    val chatLogAdapter = MutableLiveData<GroupieAdapter>(GroupieAdapter())
    val chatInputText = MutableLiveData<String>("")
    var scrollPosition = MutableLiveData<Int>(0)
    private lateinit var childEventListener: ChildEventListener
    private var selectedImageUri: Uri? = null
    private var imageSelectedType = false

    var toUser: User? = User()


    companion object {
        lateinit var currentUser: User
        var userPageToastText = MutableLiveData<String>("")
        fun printToast(text: String) {
            userPageToastText.value = text
        }
        var showImageData = MutableLiveData<Pair<ImageView, String>?>(null)
        fun showImage(imageView: ImageView, uri: String) {
            showImageData.value = Pair(imageView, uri)
        }
        fun hideImage() {
            showImageData.value = null
        }
    }


    // --------------------- LatestMessage -------------------------------------------------

    // firebase???????????????????????????????????????
    fun verifyUserIsLoggedIn(): Boolean {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            latestMessagePageEvent.value = Event("toRegister")
            return false
        }
        return true
    }

    // ???????????????????????????????????????????????????
    fun fetchCurrentUser() {
        val uid = Firebase.auth.currentUser?.uid
        FirebaseDatabase.getInstance().getReference("/users/$uid")
            .addListenerForSingleValueEvent( object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        currentUser = snapshot.getValue(User::class.java)!!
                        latestLoadingValue.value = View.GONE
                    } catch (e: Exception) {
                        latestMessagePageEvent.value = Event("toRegister")
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ????????????????????????Adapter????????????
    private fun refreshRecyclerViewMessages(addOrChangeType: String, changePosition: Int = 0, key: String = "") {
        if (addOrChangeType == "Add") {
            latestMessagesAdapter.value?.clear()
            latestOldList.clear()
            latestMessageMap.values.forEach {
                latestOldList.add(LatestMessageRow(it, addOrChangeType))
            }

            latestMessagesAdapter.value?.update(latestOldList)

        } else if (addOrChangeType == "Change") { // ?????????????????????????????????????????????????????????
            latestOldList[changePosition] = LatestMessageRow(latestMessageMap[key]!!, addOrChangeType)

            latestMessagesAdapter.value?.update(latestOldList)
        }
    }

    // ?????????????????????????????????????????????????????????????????????
    fun listenForLatestMessages() {

        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            // ???????????????????????????????????????
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages("Add")
            }

            // ?????????????????????
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

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

    // LiveData??????????????????
    fun userInfoDisplay(userImage: String = currentUser.profileImageUri,
                        userName: String = currentUser.userName,
                        userEmail: String = currentUser.userEmail) {
        editImageUri.value = userImage
        editUserNameText.value = userName
        editUserEmailText.value = userEmail
        editUserPassText.value = ""

        if (currentUser.snsLoginType) {
            passEditTextEnableType.value = false
            passEditTextHint.value = myApplication.getString(R.string.pass_mismatch_sns_login)
        } else {
            passEditTextEnableType.value = true
            passEditTextHint.value = myApplication.getString(R.string.pass_edittext_hint)
        }
    }


    // ????????????????????????????????????
    fun profileImageChange(data: Intent?, contentResolver: ContentResolver) {
        profileImageUri = data?.data!!
        bitmap.value = MediaStore.Images.Media.getBitmap(contentResolver, profileImageUri)
    }

    // ????????????????????????????????????
    fun setUpCheck() {
        listOf(editUserNameText, editUserEmailText, editUserPassText).forEach { liveData ->
            liveData.asFlow()
                    .onEach { inputTextCheck() }
                    .launchIn(viewModelScope)
        }
    }

    // ????????????????????????????????????
    private fun inputTextCheck() {
        if (currentUser.snsLoginType) {
            updateButtonType.value =
                    editUserEmailText.value?.isNotEmpty()!! &&
                    editUserEmailText.value?.isNotEmpty()!!

        } else {
            updateButtonType.value =
                    editUserEmailText.value?.isNotEmpty()!! &&
                    editUserEmailText.value?.isNotEmpty()!! &&
                    editUserPassText.value?.isNotEmpty()!!
        }
    }

    // ??????????????????????????????
    fun userProfileUpdate() {

        updateAccessLimiter = false
        progressBarType.value = View.VISIBLE

        updateProfileErrorList = mutableListOf()

        // ???????????????????????????????????????
        if (editUserNameText.value?.isEmpty()!!) {
            updateProfileErrorList.add("userName???????????????????????????")
        }
        val emailPattern = Regex("""[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+""")
        if (!emailPattern.matches(editUserEmailText.value.toString())) {
            updateProfileErrorList.add("?????????????????????????????????????????????????????????")
        }
        val passPattern = Regex("""^(?=.*?[a-z])(?=.*?\d)[a-z\d]{8,100}${'$'}""")
        if (!passPattern.matches(editUserPassText.value.toString())) {
            if (!currentUser.snsLoginType) {
                updateProfileErrorList.add("???????????????????????????8?????????????????????????????????")
            }
        }

        if (updateProfileErrorList.isNotEmpty()) {
            val errorMessage = updateProfileErrorList.joinToString(separator = "\n")
            userPageToastText.value = errorMessage
            progressBarType.value = View.GONE
            return
        }

        // ????????????

        val currentUserAuth = FirebaseAuth.getInstance().currentUser
        currentUserCopy = currentUser.copy()


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
            currentUserCopy.userName = editUserNameText.value!!
        }

        // userEmail
        if (editUserEmailText.value != currentUser.userEmail) {
            currentUserAuth?.updateEmail(editUserEmailText.value!!)
                    ?.addOnSuccessListener {
                        currentUserCopy.userEmail = editUserEmailText.value!!
                        emailUpdateProcess.value = "ok"
                    }
                    ?.addOnFailureListener {
                        emailUpdateProcess.value = "error"
                        updateProfileErrorList.add(errorSetter(it.message!!))
                    }
        } else { emailUpdateProcess.value = "ok" }

        // userPass
        if (editUserPassText.value != "") {
            currentUserAuth?.updatePassword(editUserPassText.value!!)
                    ?.addOnSuccessListener {
                        passUpdateProcess.value = "ok"
                    }
                    ?.addOnFailureListener {
                        passUpdateProcess.value = "error"
                        updateProfileErrorList.add(errorSetter(it.message!!))
                    }
        } else { passUpdateProcess.value = "ok" }


    }

    // ????????????????????????????????????????????????
    // ????????????????????????????????????????????????????????????
    suspend fun userdataUpdate() {

        if (imageUpdateProcess.value == "" || emailUpdateProcess.value == "" || passUpdateProcess.value == "" || updateAccessLimiter) {
            return
        }

        updateAccessLimiter = true

        val ref = FirebaseDatabase.getInstance().getReference("users")

        // currentUserCopy???????????????????????????????????????
        if (currentUser != currentUserCopy) {
            ref.updateChildren(mapOf(currentUser.uid to currentUserCopy))
                    .addOnSuccessListener {
                    }
        }

        withContext(Dispatchers.Main) {
            if (updateProfileErrorList.isNotEmpty()) {
                progressBarType.value = View.GONE
                // ??????????????????????????????????????????????????????
                if (updateProfileErrorList[0] == updateProfileErrorList[1]) {
                    userPageToastText.value = updateProfileErrorList[0]
                    return@withContext
                }
                val errorMessage = updateProfileErrorList.joinToString(separator = "\n")
                userPageToastText.value = errorMessage
            } else {
                progressBarType.value = View.GONE
                bitmap.value = null
                imageUpdateProcess.value = ""
                emailUpdateProcess.value = ""
                passUpdateProcess.value = ""
                updateAccessLimiter = false
                fetchCurrentUser()
                userInfoDisplay(currentUserCopy.profileImageUri, currentUserCopy.userName, currentUserCopy.userEmail)
                userPageToastText.value = "????????????"
            }
        }
    }

    // ?????????????????????????????????????????????????????????????????????
    private fun errorSetter(error: String): String {
        return when (error) {
            myApplication.getString(R.string.update_error1) ->
                "????????????????????????????????????"
            else -> "error"
        }
    }


    // ------------------------------ NewMessageActivity ------------------------------------------------

    // ?????????????????????
    fun fetchUserFriends() {
        friendList = arrayOf()
        val ref = FirebaseDatabase.getInstance().getReference("/user-friends/${currentUser.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                newMessageAdapter.value?.clear()
                snapshot.children.forEach {
                    class FriendUserUid(val uid: String = "")
//                    val userUid = it.getValue(FriendUserUid::class.java)?.uid
                    val userUid = it.value as Map<*, *>
                    if (userUid["uid"] != currentUser.uid) {

                        val friendUserRef = FirebaseDatabase.getInstance().getReference("/users/${userUid["uid"]}")
                        friendUserRef.get().addOnSuccessListener { data ->
                            val friendUser = data.getValue(User::class.java)!!
                            friendList += friendUser
                            newMessageAdapter.value?.add(UserItem(friendUser))
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // ????????????????????????
    fun addFriendFunction(friendUid: String) {

        if (currentUser.uid == friendUid) {
            userPageToastText.value = "?????????????????????????????????????????????"
            return
        } else if (friendUid.isEmpty()) {
            userPageToastText.value = "???????????????ID???????????????????????????"
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

                fetchUserFriends()

                userPageToastText.value = "${it.getValue(User::class.java)?.userName}??????????????????????????????"

            } catch (e: java.lang.Exception) {
                userPageToastText.value = "???????????????????????????????????????"
            }

        }.addOnFailureListener {
                userPageToastText.value = "???????????????????????????????????????"
        }


    }

    // ????????????????????????
    fun deleteFriendFunction(deleteList: List<Int>) {

        if (deleteList.isEmpty()) {
            userPageToastText.value = "????????????????????????????????????????????????"
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
            fetchUserFriends()
            userPageToastText.value = "??????????????????????????????"
        } catch (e: java.lang.Exception) {
            userPageToastText.value = "??????????????????????????????"
        }

    }


    // ------------------------ ChatLogActivity ---------------------------------------------------


    // ????????????????????????
    fun listenForMessages(toUserData: User?) {
        toUser = toUserData
        val fromId = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance()
                .getReference("user-messages/$fromId/${toUser?.uid}")
        val latestRef =  FirebaseDatabase.getInstance()
                .getReference("latest-messages/$fromId")

        childEventListener = ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessage = snapshot.getValue(ChatMessage::class.java)


                // alreadyRead??????????????????
                if (!chatMessage?.alreadyRead!!) {
                    chatMessage.alreadyRead = true
                }

                if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    val currentUser = currentUser
                    chatLogAdapter.value?.add(
                        ChatFromItem(chatMessage.imageUrl, chatMessage.text, currentUser)
                    )
                } else {
                    chatLogAdapter.value?.add(
                        ChatToItem(chatMessage.imageUrl, chatMessage.text, toUser!!)
                    )
                }


                // ?????????????????????????????????????????????????????????
                ref.updateChildren(mapOf(snapshot.key!! to chatMessage))
                latestRef.updateChildren(mapOf(toUser?.uid to chatMessage))

//                recyclerview_chat_log.scrollToPosition(chatLogAdapter.value?.itemCount!! - 1)
                scrollPosition.value = chatLogAdapter.value?.itemCount!! - 1

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // addChildEventListener??????????????????
    // ????????????????????????????????????????????????????????????????????????????????????
    fun eventListenerFinish() {
        val fromId = currentUser.uid
        val ref = FirebaseDatabase.getInstance()
            .getReference("user-messages/$fromId/${toUser?.uid}")
        try {
            ref.removeEventListener(childEventListener)
        } catch (e: java.lang.Exception) {
        }
    }

    // ???????????????????????????
    fun performSendMessage(imageUri: String = "") {

        val notEmptyPattern = Regex(""".+""")
        val omitToSpace = chatInputText.value!!.filter { !Regex("""\s""").matches(it.toString()) }
        if ((!notEmptyPattern.matches(omitToSpace) || omitToSpace.isEmpty()) && !imageSelectedType) {

            userPageToastText.value = "???????????????????????????????????????"
            return
        }

        imageSelectedType = false
        val fromId = FirebaseAuth.getInstance().uid!!
        val toId = toUser?.uid!!

        val reference = FirebaseDatabase.getInstance().getReference("user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("user-messages/$toId/$fromId").push()

        // ???????????????????????????????????????????????????(Activity????????????)
        val chatMessage = ChatMessage(reference.key!!, chatInputText.value!!, imageUri, fromId, toId, System.currentTimeMillis() / 1000, false)
        reference.setValue(chatMessage)
                .addOnSuccessListener {
                    chatInputText.value = ""
                    scrollPosition.value = chatLogAdapter.value?.itemCount!! - 1
                }
        toReference.setValue(chatMessage)


        val latestMessageRef = FirebaseDatabase.getInstance()
                .getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
                .getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }


    // ????????????????????????????????????????????????
    fun imageSelectedFunction(data: Intent?) {
        imageSelectedType = true
        selectedImageUri = data?.data

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/send_image/$filename")
        ref.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        performSendMessage(it.toString())
                    }
                }
    }







}