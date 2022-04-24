package com.uts.socialuts.receivers

import android.content.BroadcastReceiver
import com.uts.socialuts.providers.TokenProvider
import com.uts.socialuts.providers.NotificationProvider
import android.content.Intent
import android.app.NotificationManager
import com.uts.socialuts.providers.MessagesProvider
import com.google.gson.Gson
import com.uts.socialuts.models.FCMBody
import com.uts.socialuts.models.FCMResponse
import com.uts.socialuts.services.MyFirebaseMessagingClient
import android.app.RemoteInput
import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MessageReceiver : BroadcastReceiver() {
    var mExtraIdSender: String? = null
    var mExtraIdReceiver: String? = null
    var mExtraIdChat: String? = null
    var mExtraUsernameSender: String? = null
    var mExtraUsernameReceiver: String? = null
    var mExtraImageSender: String? = null
    var mExtraImageReceiver: String? = null
    var mExtraIdNotification = 0
    var mTokenProvider: TokenProvider? = null
    var mNotificationProvider: NotificationProvider? = null
    override fun onReceive(context: Context, intent: Intent) {
        mExtraIdSender = intent.extras!!.getString("idSender")
        mExtraIdReceiver = intent.extras!!.getString("idReceiver")
        mExtraIdChat = intent.extras!!.getString("idChat")
        mExtraUsernameSender = intent.extras!!.getString("usernameSender")
        mExtraUsernameReceiver = intent.extras!!.getString("usernameReceiver")
        mExtraImageSender = intent.extras!!.getString("imageSender")
        mExtraImageReceiver = intent.extras!!.getString("imageReceiver")
        mExtraIdNotification = intent.extras!!.getInt("idNotification")
        mTokenProvider = TokenProvider()
        mNotificationProvider = NotificationProvider()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(mExtraIdNotification)
        val message = getMessageText(intent).toString()
        sendMessage(message)
    }

    private fun sendMessage(messageText: String) {
        val message: com.uts.socialuts.models.Message =
            com.uts.socialuts.models.Message()
        message.idChat =mExtraIdChat
        message.idSender = mExtraIdReceiver
        message.idReceiver = mExtraIdSender
        message.timestamp = Date().time
        message.isViewed = false
        message.message = messageText
        val messagesProvider = MessagesProvider()
        messagesProvider.create(message).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getToken(message)
            }
        }
    }

    private fun getToken(message: com.uts.socialuts.models.Message) {
        mTokenProvider!!.getToken(mExtraIdSender)
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")) {
                        val token = documentSnapshot.getString("token")
                        val gson = Gson()
                        val messagesArray: ArrayList<com.uts.socialuts.models.Message> =
                            ArrayList<com.uts.socialuts.models.Message>()
                        messagesArray.add(message)
                        val messages = gson.toJson(messagesArray)
                        sendNotification(token, messages, message)
                    }
                }
            }
    }

    private fun sendNotification(
        token: String?,
        messages: String,
        message: com.uts.socialuts.models.Message
    ) {
        val data: MutableMap<String, String?> = HashMap()
        data["title"] = "NUEVO MENSAJE"
        data["body"] = message.message
        data["idNotification"] = mExtraIdNotification.toString()
        data["messages"] = messages
        data["usernameSender"] = mExtraUsernameReceiver!!.uppercase(Locale.getDefault())
        data["usernameReceiver"] = mExtraUsernameSender!!.uppercase(Locale.getDefault())
        data["idSender"] = message.idSender
        data["idReceiver"] = message.idReceiver
        data["idChat"] = message.idChat
        data["imageSender"] = mExtraImageReceiver
        data["imageReceiver"] = mExtraImageSender
        val body = FCMBody(token, "high", "4500s", data)
        mNotificationProvider!!.sendNotification(body).enqueue(object : Callback<FCMResponse?> {
            override fun onResponse(call: Call<FCMResponse?>, response: Response<FCMResponse?>) {}
            override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                Log.d("ERROR", "El error fue: " + t.message)
            }
        })
    }

    private fun getMessageText(intent: Intent): CharSequence? {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        return remoteInput?.getCharSequence(MyFirebaseMessagingClient.Companion.NOTIFICATION_REPLY)
    }
}