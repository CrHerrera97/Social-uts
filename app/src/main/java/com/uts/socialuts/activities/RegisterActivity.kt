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
import de.hdodenhof.circleimageview.CircleImageView
import dmax.dialog.SpotsDialog
import java.util.*
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    var mCircleImageViewBack: CircleImageView? = null
    var mTextInputUsername: TextInputEditText? = null
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    var mTextInputConfirmPassword: TextInputEditText? = null
    var mTextInputPhone: TextInputEditText? = null
    var mButtonRegister: Button? = null
    var mAuthProvider: AuthProvider? = null
    var mUsersProvider: UsersProvider? = null
    var mDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mCircleImageViewBack = findViewById(R.id.circleImageBack)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputUsername = findViewById(R.id.textInputUsername)
        mTextInputPassword = findViewById(R.id.textInputPassword)
        mTextInputConfirmPassword = findViewById(R.id.textInputConfirmPassword)
        mTextInputPhone = findViewById(R.id.textInputPhone)
        mButtonRegister = findViewById(R.id.btnRegister)
        mAuthProvider = AuthProvider()
        mUsersProvider = UsersProvider()
        mDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Espere un momento")
            .setCancelable(false).build()
        mButtonRegister!!.setOnClickListener { register() }
        mCircleImageViewBack!!.setOnClickListener { finish() }
    }

    private fun register() {
        val username: String = mTextInputUsername?.text.toString()
        val email: String = mTextInputEmail?.text.toString()
        val password: String = mTextInputPassword?.text.toString()
        val confirmPassword: String = mTextInputConfirmPassword?.text.toString()
        val phone: String = mTextInputPhone?.text.toString()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()){
            Toast.makeText(this, "Para continuar inserta todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isEmailValid(email)) {
            Toast.makeText(
                this,
                "Insertaste todos los campos pero el correo no es valido",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseña no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(
                this,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        createUser(username, email, password, phone)
    }

    private fun createUser(username: String, email: String, password: String, phone: String) {
        mDialog!!.show()
        mAuthProvider?.register(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val id: String? = mAuthProvider?.getUid()
                    val user = User()
                    user.id = id
                    user.email = email
                    user.username = username
                    user.phone = phone
                    user.timestamp = Date().time
                    mUsersProvider?.create(user)
                        ?.addOnCompleteListener { task2 ->
                            mDialog!!.dismiss()
                            if (task2.isSuccessful) {
                                val intent =
                                    Intent(this@RegisterActivity, HomeActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "No se pudo almacenar el usuario en la base de datos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(
                        this@RegisterActivity,
                        "No se pudo registrar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun isEmailValid(email: String?): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }
}