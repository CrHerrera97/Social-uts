package com.uts.socialuts.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.uts.socialuts.R
import com.uts.socialuts.models.User
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.UsersProvider
import dmax.dialog.SpotsDialog
import java.util.*

class CompleteProfileActivity : AppCompatActivity() {
    var mTextInputUsername: TextInputEditText? = null
    var mTextInputPhone: TextInputEditText? = null
    var mButtonRegister: Button? = null
    var mAuthProvider: AuthProvider? = null
    var mUsersProvider: UsersProvider? = null
    var mDialog: AlertDialog? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)
        mTextInputUsername = findViewById(R.id.textInputUsername)
        mTextInputPhone = findViewById(R.id.textInputPhone)
        mButtonRegister = findViewById(R.id.btnRegister)
        mAuthProvider = AuthProvider()
        mUsersProvider = UsersProvider()
        mDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Espere un momento")
            .setCancelable(false).build()
        mButtonRegister!!.setOnClickListener { register() }
    }

    private fun register() {
        val username: String = mTextInputUsername?.text.toString()
        val phone: String = mTextInputPhone?.text.toString()
        if (username.isNotEmpty()) {
            updateUser(username, phone)
        } else {
            Toast.makeText(this, "Para continuar inserta todos los campos", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun updateUser(username: String, phone: String) {
        val id: String? = mAuthProvider?.getUid()
        val user = User()
        user.username = username
        user.id = id
        user.phone = phone
        user.timestamp = Date().time
        mDialog!!.show()
        mUsersProvider?.update(user)?.addOnCompleteListener { task ->
            mDialog!!.dismiss()
            if (task.isSuccessful) {
                val intent = Intent(this@CompleteProfileActivity, HomeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@CompleteProfileActivity,
                    "No se pudo almacenar el usuario en la base de datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}