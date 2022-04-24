package com.uts.socialuts.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.HashMap

class MessagesProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("Messages")
    fun create(message: com.uts.socialuts.models.Message): Task<Void> {
        val document = mCollection.document()
        message.id = document.id
        return document.set(message)
    }

    fun getMessageByChat(idChat: String?): Query {
        return mCollection.whereEqualTo("idChat", idChat)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }

    fun getMessagesByChatAndSender(idChat: String?, idSender: String?): Query {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("idSender", idSender)
            .whereEqualTo("viewed", false)
    }

    fun getLastThreeMessagesByChatAndSender(idChat: String?, idSender: String?): Query {
        return mCollection
            .whereEqualTo("idChat", idChat)
            .whereEqualTo("idSender", idSender)
            .whereEqualTo("viewed", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
    }

    fun getLastMessage(idChat: String?): Query {
        return mCollection.whereEqualTo("idChat", idChat)
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getLastMessageSender(idChat: String?, idSender: String?): Query {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("idSender", idSender)
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun updateViewed(idDocument: String?, state: Boolean): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["viewed"] = state
        return mCollection.document(idDocument!!).update(map)
    }

}