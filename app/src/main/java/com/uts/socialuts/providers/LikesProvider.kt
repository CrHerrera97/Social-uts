package com.uts.socialuts.providers


import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uts.socialuts.models.Like

class LikesProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("likes")
    fun create(like: Like): Task<Void> {
        val document = mCollection.document()
        val id = document.id
        like.id = id
        return document.set(like)
    }

    fun getLikesByPost(idPost: String?): Query {
        return mCollection.whereEqualTo("idPost", idPost)
    }

    fun getLikeByPostAndUser(idPost: String?, idUser: String?): Query {
        return mCollection.whereEqualTo("idPost", idPost).whereEqualTo("idUser", idUser)
    }

    fun delete(id: String?): Task<Void> {
        return mCollection.document(id!!).delete()
    }

}