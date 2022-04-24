package com.uts.socialuts.providers

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uts.socialuts.models.Chat
import java.util.ArrayList

class ChatsProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("chats")
    fun create(chat: Chat) {
        mCollection.document(chat.idUser1 + chat.idUser2).set(chat)
    }

    fun getAll(idUser: String?): Query? {
        return idUser?.let { mCollection.whereArrayContains("ids", it) }
    }

    fun getChatByUser1AndUser2(idUser1: String, idUser2: String): Query {
        val ids = ArrayList<String>()
        ids.add(idUser1 + idUser2)
        ids.add(idUser2 + idUser1)
        return mCollection.whereIn("id", ids)
    }

}