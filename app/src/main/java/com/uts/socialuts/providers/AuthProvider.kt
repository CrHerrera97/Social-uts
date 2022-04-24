package com.uts.socialuts.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthProvider {
    private var mAuth: FirebaseAuth = Firebase.auth

    fun register(email: String?, password: String?): Task<AuthResult?>? {
        return mAuth.createUserWithEmailAndPassword(email!!, password!!)
    }

    fun login(email: String?, password: String?): Task<AuthResult?>? {
        return mAuth.signInWithEmailAndPassword(email!!, password!!)
    }

    fun googleLogin(googleSignInAccount: GoogleSignInAccount): Task<AuthResult?>? {
        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        return mAuth.signInWithCredential(credential)
    }

    fun getEmail(): String? {
        return if (mAuth.currentUser != null) {
            mAuth.currentUser!!.email
        } else {
            null
        }
    }

    fun getUid(): String? {
        return if (mAuth.currentUser != null) {
            mAuth.currentUser!!.uid
        } else {
            null
        }
    }

    fun getUserSession(): FirebaseUser? {
        return if (mAuth.currentUser != null) {
            mAuth.currentUser
        } else {
            null
        }
    }

    fun logout() {
        mAuth.signOut()
    }
}