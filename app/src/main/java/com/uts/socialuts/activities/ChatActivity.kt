package com.uts.socialuts.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.adapters.MessagesAdapter
import com.uts.socialuts.models.Chat
import com.uts.socialuts.models.FCMBody
import com.uts.socialuts.models.FCMResponse
import com.uts.socialuts.models.Message
import com.uts.socialuts.providers.*
import com.uts.socialuts.utils.RelativeTime
import com.uts.socialuts.utils.ViewedMessageHelper
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ChatActivity : AppCompatActivity() {
    var mExtraIdUser1: String? = null
    var mExtraIdUser2: String? = null
    var mExtraIdChat: String? = null
    var mIdNotificationChat: Long = 0
    var mChatsProvider: ChatsProvider? = null
    var mMessagesProvider: MessagesProvider? = null
    var mUsersProvider: UsersProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mNotificationProvider: NotificationProvider? = null
    var mTokenProvider: TokenProvider? = null
    var mEditTextMessage: EditText? = null
    var mImageViewSendMessage: ImageView? = null
    var mCircleImageProfile: CircleImageView? = null
    var mTextViewUsername: TextView? = null
    var mTextViewRelativeTime: TextView? = null
    var mImageViewBack: ImageView? = null
    var mRecyclerViewMessage: RecyclerView? = null
    var mAdapter: MessagesAdapter? = null
    var mActionBarView: View? = null
    var mLinearLayoutManager: LinearLayoutManager? = null
    var mListener: ListenerRegistration? = null
    var mMyUsername: String? = null
    var mUsernameChat: String? = null
    var mImageReceiver: String? = ""
    var mImageSender = ""
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mChatsProvider = ChatsProvider()
        mMessagesProvider = MessagesProvider()
        mAuthProvider = AuthProvider()
        mUsersProvider = UsersProvider()
        mNotificationProvider = NotificationProvider()
        mTokenProvider = TokenProvider()
        mEditTextMessage = findViewById(R.id.editTextMessage)
        mImageViewSendMessage = findViewById(R.id.imageViewSendMessage)
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessage)
        mLinearLayoutManager = LinearLayoutManager(this@ChatActivity)
        mLinearLayoutManager!!.stackFromEnd = true
        mRecyclerViewMessage?.layoutManager = mLinearLayoutManager
        mExtraIdUser1 = intent.getStringExtra("idUser1")
        mExtraIdUser2 = intent.getStringExtra("idUser2")
        mExtraIdChat = intent.getStringExtra("idChat")
        showCustomToolbar(R.layout.custom_chat_toolbar)
        getMyInfoUser()
        mImageViewSendMessage!!.setOnClickListener { sendMessage() }
        checkIfChatExist()
    }

    override fun onStart() {
        super.onStart()
        mAdapter?.startListening()
        ViewedMessageHelper.updateOnline(true, this@ChatActivity)
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@ChatActivity)
    }

    override fun onStop() {
        super.onStop()
        mAdapter?.stopListening()
    }

    protected override fun onDestroy() {
        super.onDestroy()
        mListener?.remove()
    }

    private fun getMessageChat() {
        val query: Query? = mMessagesProvider?.getMessageByChat(mExtraIdChat)
        val options: FirestoreRecyclerOptions<Message?> = query?.let {
            FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(it, Message::class.java)
                .build()
        } as FirestoreRecyclerOptions<Message?>
        mAdapter = MessagesAdapter(options, this@ChatActivity)
        mRecyclerViewMessage?.adapter = mAdapter
        mAdapter!!.startListening()
        mAdapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                updateViewed()
                val numberMessage: Int = mAdapter!!.itemCount
                val lastMessagePosition: Int? = mLinearLayoutManager?.findLastCompletelyVisibleItemPosition()

                if (lastMessagePosition == -1 || positionStart >= numberMessage - 1 && lastMessagePosition == positionStart - 1) {
                    mRecyclerViewMessage?.scrollToPosition(positionStart)
                }
            }
        })
    }

    private fun sendMessage() {
        val textMessage: String = mEditTextMessage?.text.toString()
        if (!textMessage.isEmpty()) {
            val message = Message()
            message.idChat = mExtraIdChat
            if (mAuthProvider?.getUid().equals(mExtraIdUser1)) {
                message.idSender = mExtraIdUser1
                message.idReceiver = mExtraIdUser2
            } else {
                message.idSender = mExtraIdUser2
                message.idReceiver = mExtraIdUser1
            }
            message.timestamp = Date().time
            message.isViewed = false
            message.idChat = mExtraIdChat
            message.message = textMessage
            mMessagesProvider?.create(message)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mEditTextMessage?.setText("")
                        mAdapter?.notifyDataSetChanged()
                        getToken(message)
                    } else {
                        Toast.makeText(
                            this@ChatActivity,
                            "El mensaje no se pudo crear",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun showCustomToolbar(resource: Int) {
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = ""
        actionBar?.setDisplayShowHomeEnabled(true)
        actionBar?.setDisplayShowCustomEnabled(true)
        val inflater: LayoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mActionBarView = inflater.inflate(resource, null)
        actionBar?.customView = mActionBarView
        mCircleImageProfile = mActionBarView!!.findViewById(R.id.circleImageProfile)
        mTextViewUsername = mActionBarView!!.findViewById(R.id.textViewUsername)
        mTextViewRelativeTime = mActionBarView!!.findViewById(R.id.textViewRelativeTime)
        mImageViewBack = mActionBarView!!.findViewById(R.id.imageViewBack)
        mImageViewBack?.setOnClickListener { finish() }
        getUserInfo()
    }

    private fun getUserInfo() {
        var idUserInfo: String? = ""
        idUserInfo = if (mAuthProvider?.getUid().equals(mExtraIdUser1)) {
            mExtraIdUser2
        } else {
            mExtraIdUser1
        }
        mListener = mUsersProvider?.getUserRealtime(idUserInfo)
            ?.addSnapshotListener { documentSnapshot, e ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            mUsernameChat = documentSnapshot.getString("username")
                            mTextViewUsername?.text = mUsernameChat
                        }
                        if (documentSnapshot.contains("online")) {
                            val online: Boolean = (documentSnapshot.getBoolean("online") == true)
                            if (online) {
                                mTextViewRelativeTime?.text = "En linea"
                            } else if (documentSnapshot.contains("lastConnect")) {
                                val lastConnect: Long? = documentSnapshot.getLong("lastConnect")
                                val relativeTime: String? =
                                    lastConnect?.let { RelativeTime.getTimeAgo(it, this@ChatActivity) }
                                mTextViewRelativeTime?.text = relativeTime
                            }
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            mImageReceiver = documentSnapshot.getString("image_profile")
                            if (mImageReceiver != null) {
                                if (mImageReceiver != "") {
                                    Picasso.with(this@ChatActivity).load(mImageReceiver)
                                        .into(mCircleImageProfile)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun checkIfChatExist() {
        mExtraIdUser1?.let {
            mExtraIdUser2?.let { it1 ->
                mChatsProvider?.getChatByUser1AndUser2(it, it1)?.get()
                    ?.addOnSuccessListener { queryDocumentSnapshots ->
                        val size: Int = queryDocumentSnapshots.size()
                        if (size == 0) {
                            createChat()
                        } else {
                            mExtraIdChat = queryDocumentSnapshots.documents[0].id
                            mIdNotificationChat =
                                queryDocumentSnapshots.documents[0].getLong("idNotification")!!
                            getMessageChat()
                            updateViewed()
                        }
                    }
            }
        }
    }

    private fun updateViewed() {
        var idSender: String? = ""
        idSender = if (mAuthProvider?.getUid().equals(mExtraIdUser1)) {
            mExtraIdUser2
        } else {
            mExtraIdUser1
        }
        mMessagesProvider?.getMessagesByChatAndSender(mExtraIdChat, idSender)?.get()
            ?.addOnSuccessListener { queryDocumentSnapshots ->
                for (document in queryDocumentSnapshots.documents) {
                    mMessagesProvider?.updateViewed(document.id, true)
                }
            }
    }

    private fun createChat() {
        val chat = Chat()
        chat.idUser1 = mExtraIdUser1
        chat.idUser2 = mExtraIdUser2
        chat.isWriting = false
        chat.timestamp = Date().time
        chat.id = (mExtraIdUser1 + mExtraIdUser2)
        val random = Random()
        val n = random.nextInt(1000000)
        chat.idNotification = n
        mIdNotificationChat = n.toLong()
        val ids = ArrayList<String>()
        mExtraIdUser1?.let { ids.add(it) }
        mExtraIdUser2?.let { ids.add(it) }
        chat.ids = ids
        mChatsProvider?.create(chat)
        mExtraIdChat = chat.id
        getMessageChat()
    }

    private fun getToken(message: Message) {
        var idUser: String? = ""
        idUser = if (mAuthProvider?.getUid().equals(mExtraIdUser1)) {
            mExtraIdUser2
        } else {
            mExtraIdUser1
        }
        mTokenProvider?.getToken(idUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")) {
                        val token: String? = documentSnapshot.getString("token")
                        if (token != null) {
                            getLastThreeMessages(message, token)
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ChatActivity,
                        "El token de notificaciones del usuario no existe",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun getLastThreeMessages(message: Message, token: String) {
        mMessagesProvider?.getLastThreeMessagesByChatAndSender(mExtraIdChat,
            mAuthProvider?.getUid()
        )
            ?.get()?.addOnSuccessListener { queryDocumentSnapshots ->
                val messageArrayList: ArrayList<Message?> = ArrayList<Message?>()
                for (d in queryDocumentSnapshots.documents) {
                    if (d.exists()) {
                        val message: Message? = d.toObject(Message::class.java)
                        messageArrayList.add(message)
                    }
                }
                if (messageArrayList.size == 0) {
                    messageArrayList.add(message)
                }
                messageArrayList.reverse()
                val gson = Gson()
                val messages: String = gson.toJson(messageArrayList)
                sendNotification(token, messages, message)
            }
    }

    private fun sendNotification(token: String, messages: String, message: Message) {
        val data: MutableMap<String, String?> = HashMap()
        data["title"] = "NUEVO MENSAJE"
        data["body"] = message.message
        data["idNotification"] = mIdNotificationChat.toString()
        data["messages"] = messages
        data["usernameSender"] = mMyUsername!!.uppercase(Locale.getDefault())
        data["usernameReceiver"] = mUsernameChat!!.uppercase(Locale.getDefault())
        data["idSender"] = message.idSender
        data["idReceiver"] = message.idReceiver
        data["idChat"] = message.idChat
        if (mImageSender == "") {
            mImageSender = "IMAGEN_NO_VALIDA"
        }
        if (mImageReceiver == "") {
            mImageReceiver = "IMAGEN_NO_VALIDA"
        }
        data["imageSender"] = mImageSender
        data["imageReceiver"] = mImageReceiver
        var idSender: String? = ""
        idSender = if (mAuthProvider?.getUid().equals(mExtraIdUser1)) {
            mExtraIdUser2
        } else {
            mExtraIdUser1
        }
        mMessagesProvider?.getLastMessageSender(mExtraIdChat, idSender)?.get()
            ?.addOnSuccessListener { queryDocumentSnapshots ->
                val size: Int = queryDocumentSnapshots.size()
                var lastMessage = ""
                if (size > 0) {
                    lastMessage =
                        queryDocumentSnapshots.documents[0].getString("message").toString()
                    data["lastMessage"] = lastMessage
                }
                val body = FCMBody(token, "high", "4500s", data)
                mNotificationProvider?.sendNotification(body)
                    ?.enqueue(object : Callback<FCMResponse?> {
                        override fun onResponse(
                            call: Call<FCMResponse?>,
                            response: Response<FCMResponse?>
                        ) {
                            if (response.body() != null) {
                                if (response.body()!!.success === 1) {
                                    //Toast.makeText(ChatActivity.this, "La notificacion se envio correcatemente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(
                                        this@ChatActivity,
                                        "La notificacion no se pudo enviar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@ChatActivity,
                                    "La notificacion no se pudo enviar",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {}
                    })
            }
    }

    private fun getMyInfoUser() {
        mUsersProvider?.getUser(mAuthProvider?.getUid())
            ?.addOnSuccessListener(OnSuccessListener<DocumentSnapshot?> { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mMyUsername = documentSnapshot.getString("username")
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageSender = documentSnapshot.getString("image_profile").toString()
                    }
                }
            })
    }
}