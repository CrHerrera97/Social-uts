package com.uts.socialuts.providers

import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.uts.socialuts.models.Token

class TokenProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("tokens")
    fun create(idUser: String?) {
        if (idUser == null) {
            return
        }
        Firebase.messaging.token.addOnSuccessListener { result ->
            val token = Token(result)
            mCollection.document(idUser).set(token)
        }
    }

    fun getToken(idUser: String?): Task<DocumentSnapshot> {
        return mCollection.document(idUser!!).get()
    }

}