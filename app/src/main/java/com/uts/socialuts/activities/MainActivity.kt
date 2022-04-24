package com.uts.socialuts.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.uts.socialuts.R
import com.uts.socialuts.models.User
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.UsersProvider
import dmax.dialog.SpotsDialog

class MainActivity : AppCompatActivity() {
    var mTextViewRegister: TextView? = null
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    var mButtonLogin: Button? = null
    var mAuthProvider: AuthProvider? = null
    var mButtonGoogle: SignInButton? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var mUsersProvider: UsersProvider? = null
    var termsCB: CheckBox? = null
    private val REQUEST_CODE_GOOGLE = 1
    var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mTextViewRegister = findViewById(R.id.textViewRegister)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputPassword = findViewById(R.id.textInputPassword)
        mButtonLogin = findViewById(R.id.btnLogin)
        mButtonGoogle = findViewById(R.id.btnLoginGoogle)
        termsCB = findViewById(R.id.checkBox)
        mAuthProvider = AuthProvider()
        mDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Espere un momento")
            .setCancelable(false).build()

        termsCB?.movementMethod  = LinkMovementMethod.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mUsersProvider = UsersProvider()
        mButtonGoogle?.setOnClickListener { signInGoogle() }
        mButtonLogin!!.setOnClickListener { login() }
        mTextViewRegister?.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    protected override fun onStart() {
        super.onStart()
        if (mAuthProvider?.getUserSession() != null) {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun signInGoogle() {
        if (termsCB?.isChecked() != true){
            Toast.makeText(
                this@MainActivity,
                "Debe aceptar términos y condiciones",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("ERROR", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        mDialog!!.show()
        if (acct != null) {
            mAuthProvider?.googleLogin(acct)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val id: String? = mAuthProvider?.getUid()
                        if (id != null) {
                            checkUserExist(id)
                        }
                    } else {
                        mDialog!!.dismiss()
                        // If sign in fails, display a message to the user.
                        Log.w("ERROR", "signInWithCredential:failure", task.exception)
                        Toast.makeText(
                            this@MainActivity,
                            "No se pudo iniciar sesion con google",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun checkUserExist(id: String) {
        mUsersProvider?.getUser(id)
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        mDialog!!.dismiss()
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        val email: String? = mAuthProvider?.getEmail()
                        val user = User()
                        user.email = email
                        user.id = id
                        mUsersProvider?.create(user)
                            ?.addOnCompleteListener { task ->
                                mDialog!!.dismiss()
                                if (task.isSuccessful) {
                                    val intent = Intent(
                                        this@MainActivity,
                                        CompleteProfileActivity::class.java
                                    )
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "No se pudo almacenar la informacion del usuario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
    }

    private fun login() {
        val email: String = mTextInputEmail?.text.toString()
        val password: String = mTextInputPassword?.text.toString()
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(
                this@MainActivity,
                "No se proporcionó el email o la contraseña",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (termsCB?.isChecked() != true){
            Toast.makeText(
                this@MainActivity,
                "Debe aceptar términos y condiciones",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        mDialog!!.show()
        mAuthProvider?.login(email, password)
            ?.addOnCompleteListener { task ->
                mDialog!!.dismiss()
                if (!task.isSuccessful){
                    Toast.makeText(
                        this@MainActivity,
                        "El email o la contraseña que ingresaste no son correctas",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        Log.d("CAMPO", "email: $email")
        Log.d("CAMPO", "password: $password")
    }
}