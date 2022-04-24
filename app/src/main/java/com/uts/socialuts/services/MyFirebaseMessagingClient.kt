package com.uts.socialuts.services

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.squareup.picasso.Target
import com.uts.socialuts.channel.NotificationHelper
import com.uts.socialuts.receivers.MessageReceiver
import com.google.gson.Gson
import java.util.*

class MyFirebaseMessagingClient : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data: Map<String, String> = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        if (title != null) {
            if (title == "NUEVO MENSAJE") {
                showNotificationMessage(data)
            } else {
                showNotification(title, body)
            }
        }
    }

    private fun showNotification(title: String, body: String?) {
        val notificationHelper = NotificationHelper(baseContext)
        val builder: NotificationCompat.Builder? = notificationHelper.getNotification(title, body)
        val random = Random()
        val n = random.nextInt(10000)
        if (builder != null) {
            notificationHelper.getManager()?.notify(n, builder.build())
        }
    }

    private fun showNotificationMessage(data: Map<String, String>) {
        val imageSender = data["imageSender"]
        val imageReceiver = data["imageReceiver"]
        Log.d("ENTRO", "NUEVO MENSAJE")
        getImageSender(data, imageSender, imageReceiver)
    }

    private fun getImageSender(
        data: Map<String, String>,
        imageSender: String?,
        imageReceiver: String?
    ) {
        Handler(Looper.getMainLooper())
            .post {
                Picasso.with(applicationContext)
                    .load(imageSender)
                    .into(object : Target {
                        override fun onBitmapLoaded(
                            bitmapSender: Bitmap,
                            from: Picasso.LoadedFrom
                        ) {
                            getImageReceiver(data, imageReceiver, bitmapSender)
                        }

                        override fun onBitmapFailed(errorDrawable: Drawable) {
                            getImageReceiver(data, imageReceiver, null)
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                    })
            }
    }

    private fun getImageReceiver(
        data: Map<String, String>,
        imageReceiver: String?,
        bitmapSender: Bitmap?
    ) {
        Picasso.with(applicationContext)
            .load(imageReceiver)
            .into(object : Target {
                override fun onBitmapLoaded(bitmapReceiver: Bitmap, from: Picasso.LoadedFrom) {
                    notifyMessage(data, bitmapSender, bitmapReceiver)
                }

                override fun onBitmapFailed(errorDrawable: Drawable) {
                    notifyMessage(data, bitmapSender, null)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
            })
    }

    private fun notifyMessage(
        data: Map<String, String>,
        bitmapSender: Bitmap?,
        bitmapReceiver: Bitmap?
    ) {
        val usernameSender = data["usernameSender"]
        val usernameReceiver = data["usernameReceiver"]
        val lastMessage = data["lastMessage"]
        val messagesJSON = data["messages"]
        val imageSender = data["imageSender"]
        val imageReceiver = data["imageReceiver"]
        val idSender = data["idSender"]
        val idReceiver = data["idReceiver"]
        val idChat = data["idChat"]
        val idNotification = data["idNotification"]!!.toInt()
        val intent = Intent(this, MessageReceiver::class.java)
        intent.putExtra("idSender", idSender)
        intent.putExtra("idReceiver", idReceiver)
        intent.putExtra("idChat", idChat)
        intent.putExtra("idNotification", idNotification)
        intent.putExtra("usernameSender", usernameSender)
        intent.putExtra("usernameReceiver", usernameReceiver)
        intent.putExtra("imageSender", imageSender)
        intent.putExtra("imageReceiver", imageReceiver)
        val pendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteInput = RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Tu mensaje...").build()
        val action: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            "Responder",
            pendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
        val gson = Gson()
        val messages: Array<com.uts.socialuts.models.Message> =
            gson.fromJson<Array<com.uts.socialuts.models.Message>>(
                messagesJSON,
                Array<com.uts.socialuts.models.Message>::class.java
            )
        val notificationHelper = NotificationHelper(baseContext)
        val builder: NotificationCompat.Builder? = notificationHelper.getNotificationMessage(
            messages,
            usernameSender,
            usernameReceiver,
            lastMessage,
            bitmapSender,
            bitmapReceiver,
            action
        )
        if (builder != null) {
            notificationHelper.getManager()?.notify(idNotification, builder.build())
        }
    }

    companion object {
        const val NOTIFICATION_REPLY = "NotificationReply"
    }
}