package com.uts.socialuts.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uts.socialuts.models.Post

class PostProvider {
    var mCollection: CollectionReference = Firebase.firestore.collection("Posts")
    fun save(post: Post?): Task<Void> {
        return mCollection.document().set(post!!)
    }

    fun getAll(): Query {
        return mCollection.orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getPostByCategoryAndTimestamp(category: String?): Query {
        return mCollection.whereEqualTo("category", category)
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun getPostByTitle(title: String): Query {
        return mCollection.orderBy("title").startAt(title).endAt(title + '\uf8ff')
    }

    fun getPostByUser(id: String?): Query {
        return mCollection.whereEqualTo("idUser", id)
    }

    fun getPostById(id: String?): Task<DocumentSnapshot> {
        return mCollection.document(id!!).get()
    }

    fun delete(id: String?): Task<Void> {
        return mCollection.document(id!!).delete()
    }

}