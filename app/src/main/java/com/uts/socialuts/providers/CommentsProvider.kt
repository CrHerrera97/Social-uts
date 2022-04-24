package com.uts.socialuts.providers


import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommentsProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("comments")
    fun create(comment: com.uts.socialuts.models.Comment?): Task<Void> {
        return mCollection.document().set(comment!!)
    }

    fun getCommentsByPost(idPost: String?): Query {
        return mCollection.whereEqualTo("idPost", idPost)
    }

}