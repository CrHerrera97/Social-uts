package com.uts.socialuts.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.activities.ChatActivity
import com.uts.socialuts.models.Chat
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.ChatsProvider
import com.uts.socialuts.providers.MessagesProvider
import com.uts.socialuts.providers.UsersProvider
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatsAdapter(options: FirestoreRecyclerOptions<Chat?>, var context: Context) :
    FirestoreRecyclerAdapter<Chat?, ChatsAdapter.ViewHolder?>(options) {
    var mUsersProvider: UsersProvider = UsersProvider()
    var mAuthProvider: AuthProvider = AuthProvider()
    var mChatsProvider: ChatsProvider = ChatsProvider()
    var mMessagesProvider: MessagesProvider = MessagesProvider()
    var mListener: ListenerRegistration? = null
    var mListenerLastMessage: ListenerRegistration? = null
    protected override fun onBindViewHolder(holder: ViewHolder, position: Int, chat: Chat) {
        val document: DocumentSnapshot = snapshots.getSnapshot(position)
        val chatId: String = document.id
        if (mAuthProvider.getUid().equals(chat.idUser1)) {
            chat.idUser2?.let { getUserInfo(it, holder) }
        } else {
            chat.idUser1?.let { getUserInfo(it, holder) }
        }
        holder.viewHolder.setOnClickListener {
            chat.idUser2?.let { it1 ->
                chat.idUser1?.let { it2 ->
                    goToChatActivity(
                        chatId,
                        it2,
                        it1
                    )
                }
            }
        }
        getLastMessage(chatId, holder.textViewLastMessage)
        var idSender = ""
        idSender = if (mAuthProvider.getUid().equals(chat.idUser1)) ({
            chat.idUser2
        }).toString() else ({
            chat.idUser1
        }).toString()
        getMessageNotRead(
            chatId,
            idSender,
            holder.textViewMessageNotRead,
            holder.frameLayoutMessageNotRead
        )
    }

    private fun getMessageNotRead(
        chatId: String,
        idSender: String,
        textViewMessageNotRead: TextView,
        frameLayoutMessageNotRead: FrameLayout
    ) {
        mListener = mMessagesProvider.getMessagesByChatAndSender(chatId, idSender).addSnapshotListener { queryDocumentSnapshots, e ->
            if (queryDocumentSnapshots != null) {
                val size: Int = queryDocumentSnapshots.size()
                if (size > 0) {
                    frameLayoutMessageNotRead.visibility = View.VISIBLE
                    textViewMessageNotRead.text = size.toString()
                } else {
                    frameLayoutMessageNotRead.visibility = View.GONE
                }
            }
        }
    }

    fun getListener(): ListenerRegistration? {
        return mListener
    }

    fun getListenerLastMessage(): ListenerRegistration? {
        return mListenerLastMessage
    }
    private fun getLastMessage(chatId: String, textViewLastMessage: TextView) {
        mListenerLastMessage = mMessagesProvider.getLastMessage(chatId).addSnapshotListener{queryDocumentSnapshots, e ->
                    if (queryDocumentSnapshots != null) {
                        val size: Int = queryDocumentSnapshots.size()
                        if (size > 0) {
                            val lastMessage: String? =
                                queryDocumentSnapshots.documents[0].getString("message")
                            textViewLastMessage.text = lastMessage
                        }
                    }
                }

    }

    private fun goToChatActivity(chatId: String, idUser1: String, idUser2: String) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("idChat", chatId)
        intent.putExtra("idUser1", idUser1)
        intent.putExtra("idUser2", idUser2)
        context.startActivity(intent)
    }

    private fun getUserInfo(idUser: String, holder: ViewHolder) {
        mUsersProvider.getUser(idUser)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            val username: String? = documentSnapshot.getString("username")
                            holder.textViewUsername.text = username?.uppercase(Locale.getDefault())
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            val imageProfile: String? = documentSnapshot.getString("image_profile")
                            if (imageProfile != null) {
                                if (imageProfile.isNotEmpty()) {
                                    Picasso.with(context).load(imageProfile)
                                        .into(holder.circleImageChat)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_chats, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textViewUsername: TextView = view.findViewById(R.id.textViewUsernameChat)
        var textViewLastMessage: TextView = view.findViewById(R.id.textViewLastMessageChat)
        var textViewMessageNotRead: TextView = view.findViewById(R.id.textViewMessageNotRead)
        var circleImageChat: CircleImageView = view.findViewById(R.id.circleImageChat)
        var frameLayoutMessageNotRead: FrameLayout = view.findViewById(R.id.frameLayoutMessageNotRead)
        var viewHolder: View = view
    }

}