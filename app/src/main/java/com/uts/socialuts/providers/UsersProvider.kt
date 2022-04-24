package com.uts.socialuts.providers

import com.google.firebase.firestore.DocumentSnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.uts.socialuts.models.User
import java.util.*

class UsersProvider {

    private var mCollection: CollectionReference? = null

    init {
        val db = Firebase.firestore
        mCollection = db.collection("users")
    }

    fun getUser(id: String?): Task<DocumentSnapshot?> {
        return mCollection!!.document(id!!).get()
    }

    fun getUserRealtime(id: String?): DocumentReference {
        return mCollection!!.document(id!!)
    }

    fun create(user: User): Task<Void?>? {
        return user.id?.let { mCollection!!.document(it).set(user) }
    }

    fun update(user: User): Task<Void?>? {
        val map: MutableMap<String, Any?> = HashMap()
        user.username.also { map["username"] = it }
        user.phone.also { map["phone"] = it }
        map["timestamp"] = Date().time.toString()
        user.imageProfile.also { map["image_profile"] = it }
        user.imageCover.also { map["image_cover"] = it }
        return user.id?.let { mCollection!!.document(it).update(map) }
    }

    fun updateOnline(idUser: String?, status: Boolean): Task<Void?> {
        val map: MutableMap<String, Any> = HashMap()
        map["online"] = status
        map["lastConnect"] = Date().time
        return mCollection!!.document(idUser!!).update(map)
    }
}