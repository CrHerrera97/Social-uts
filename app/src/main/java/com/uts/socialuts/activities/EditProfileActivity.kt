package com.uts.socialuts.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.models.User
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.ImageProvider
import com.uts.socialuts.providers.UsersProvider
import com.uts.socialuts.utils.FileUtil
import com.uts.socialuts.utils.ViewedMessageHelper
import de.hdodenhof.circleimageview.CircleImageView
import dmax.dialog.SpotsDialog
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    var mCircleImageViewBack: CircleImageView? = null
    var mCircleImageViewProfile: CircleImageView? = null
    var mImageViewCover: ImageView? = null
    var mTextInputUsername: TextInputEditText? = null
    var mTextInputPhone: TextInputEditText? = null
    var mButtonEditProfile: Button? = null
    var mBuilderSelector: AlertDialog.Builder? = null
    lateinit var options: Array<CharSequence>
    private val GALLERY_REQUEST_CODE_PROFILE = 1
    private val GALLERY_REQUEST_CODE_COVER = 2
    private val PHOTO_REQUEST_CODE_PROFILE = 3
    private val PHOTO_REQUEST_CODE_COVER = 4

    // FOTO 1
    var mAbsolutePhotoPath: String? = null
    var mPhotoPath: String? = null
    var mPhotoFile: File? = null

    // FOTO 2
    var mAbsolutePhotoPath2: String? = null
    var mPhotoPath2: String? = null
    var mPhotoFile2: File? = null
    var mImageFile: File? = null
    var mImageFile2: File? = null
    var mUsername = ""
    var mPhone = ""
    var mImageProfile: String? = ""
    var mImageCover: String? = ""
    var mDialog: AlertDialog? = null
    var mImageProvider: ImageProvider? = null
    var mUsersProvider: UsersProvider? = null
    var mAuthProvider: AuthProvider? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        mCircleImageViewBack = findViewById(R.id.circleImageBack)
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile)
        mImageViewCover = findViewById(R.id.imageViewCover)
        mTextInputUsername = findViewById(R.id.textInputUsername)
        mTextInputPhone = findViewById(R.id.textInputPhone)
        mButtonEditProfile = findViewById(R.id.btnEditProfile)
        mBuilderSelector = AlertDialog.Builder(this)
        mBuilderSelector!!.setTitle("Selecciona una opcion")
        options = arrayOf("Imagen de galeria", "Tomar foto")
        mImageProvider = ImageProvider()
        mUsersProvider = UsersProvider()
        mAuthProvider = AuthProvider()
        mDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Espere un momento")
            .setCancelable(false).build()
        mButtonEditProfile!!.setOnClickListener { clickEditProfile() }
        mCircleImageViewProfile!!.setOnClickListener { selectOptionImage(1) }
        mImageViewCover!!.setOnClickListener { selectOptionImage(2) }
        mCircleImageViewBack!!.setOnClickListener { finish() }
        getUser()
    }

    private fun getUser() {
        mUsersProvider?.getUser(mAuthProvider?.getUid())
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null) {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("username")) {
                            mUsername = documentSnapshot.getString("username").toString()
                            mTextInputUsername?.setText(mUsername)
                        }
                        if (documentSnapshot.contains("phone")) {
                            mPhone = documentSnapshot.getString("phone").toString()
                            mTextInputPhone?.setText(mPhone)
                        }
                        if (documentSnapshot.contains("image_profile")) {
                            mImageProfile = documentSnapshot.getString("image_profile")
                            if (mImageProfile != null) {
                                if (!mImageProfile!!.isEmpty()) {
                                    Picasso.with(this@EditProfileActivity).load(mImageProfile)
                                        .into(mCircleImageViewProfile)
                                }
                            }
                        }
                        if (documentSnapshot.contains("image_cover")) {
                            mImageCover = documentSnapshot.getString("image_cover")
                            if (mImageCover != null) {
                                if (mImageCover!!.isNotEmpty()) {
                                    Picasso.with(this@EditProfileActivity).load(mImageCover)
                                        .into(mImageViewCover)
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun clickEditProfile() {
        mUsername = mTextInputUsername?.text.toString()
        mPhone = mTextInputPhone?.text.toString()
        if (mUsername.isEmpty() || mPhone.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre de usuario y el telefono", Toast.LENGTH_SHORT).show()
            return
        }
        if (mImageFile != null && mImageFile2 != null) {
            saveImageCoverAndProfile(mImageFile!!, mImageFile2!!)
        } else if (mPhotoFile != null && mPhotoFile2 != null) {
            saveImageCoverAndProfile(mPhotoFile!!, mPhotoFile2!!)
        } else if (mImageFile != null && mPhotoFile2 != null) {
            saveImageCoverAndProfile(mImageFile!!, mPhotoFile2!!)
        } else if (mPhotoFile != null && mImageFile2 != null) {
            saveImageCoverAndProfile(mPhotoFile!!, mImageFile2!!)
        } else if (mPhotoFile != null) {
            saveImage(mPhotoFile!!, true)
        } else if (mPhotoFile2 != null) {
            saveImage(mPhotoFile2!!, false)
        } else if (mImageFile != null) {
            saveImage(mImageFile!!, true)
        } else if (mImageFile2 != null) {
            saveImage(mImageFile2!!, false)
        } else {
            val user = User()
            user.username = mUsername
            user.phone = mPhone
            user.id = mAuthProvider?.getUid()
            updateInfo(user)
        }
    }

    private fun saveImageCoverAndProfile(imageFile1: File, imageFile2: File) {
        mDialog!!.show()
        mImageProvider?.save(this@EditProfileActivity, imageFile1)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mImageProvider?.storage?.downloadUrl
                        ?.addOnSuccessListener { uri ->
                            val urlProfile = uri.toString()
                            mImageProvider!!.save(this@EditProfileActivity, imageFile2)
                                .addOnCompleteListener { taskImage2 ->
                                    if (taskImage2.isSuccessful) {
                                        mImageProvider?.storage?.downloadUrl!!
                                            .addOnSuccessListener { uri2 ->
                                                val urlCover = uri2.toString()
                                                val user = User()
                                                user.username = mUsername
                                                user.phone = mPhone
                                                user.imageProfile = urlProfile
                                                user.imageCover = urlCover
                                                user.id = mAuthProvider?.getUid()
                                                updateInfo(user)
                                            }
                                    } else {
                                        mDialog!!.dismiss()
                                        Toast.makeText(
                                            this@EditProfileActivity,
                                            "La imagen numero 2 no se pudo guardar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Hubo error al almacenar la imagen",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveImage(image: File, isProfileImage: Boolean) {
        mDialog!!.show()
        mImageProvider?.save(this@EditProfileActivity, image)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mImageProvider?.storage?.downloadUrl
                        ?.addOnSuccessListener { uri ->
                            val url = uri.toString()
                            val user = User()
                            user.username = mUsername
                            user.phone = mPhone
                            if (isProfileImage) {
                                user.imageProfile = url
                                user.imageCover = mImageCover
                            } else {
                                user.imageCover = url
                                user.imageProfile = mImageProfile
                            }
                            user.id = mAuthProvider?.getUid()
                            updateInfo(user)
                        }
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Hubo error al almacenar la imagen",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun updateInfo(user: User) {
        if (mDialog!!.isShowing) {
            mDialog!!.show()
        }
        mUsersProvider?.update(user)?.addOnCompleteListener { task ->
            mDialog!!.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(
                    this@EditProfileActivity,
                    "La informacion se actualizo correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                goToHome()
            } else {
                Toast.makeText(
                    this@EditProfileActivity,
                    "La informacion no se pudo actualizar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun goToHome(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun selectOptionImage(numberImage: Int) {
        mBuilderSelector?.setItems(options) { _, i ->
            if (i == 0) {
                if (numberImage == 1) {
                    openGallery(GALLERY_REQUEST_CODE_PROFILE)
                } else if (numberImage == 2) {
                    openGallery(GALLERY_REQUEST_CODE_COVER)
                }
            } else if (i == 1) {
                if (numberImage == 1) {
                    takePhoto(PHOTO_REQUEST_CODE_PROFILE)
                } else if (numberImage == 2) {
                    takePhoto(PHOTO_REQUEST_CODE_COVER)
                }
            }
        }
        mBuilderSelector!!.show()
    }

    private fun takePhoto(requestCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createPhotoFile(requestCode)
            } catch (e: Exception) {
                Toast.makeText(this, "Hubo un error con el archivo " + e.message, Toast.LENGTH_LONG)
                    .show()
            }
            if (photoFile != null) {
                val photoUri: Uri = FileProvider.getUriForFile(
                    this@EditProfileActivity,
                    "com.uts.socialuts",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, requestCode)
            }
        }
    }

    @Throws(IOException::class)
    private fun createPhotoFile(requestCode: Int): File {
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File.createTempFile(
            Date().toString() + "_photo",
            ".jpg",
            storageDir
        )
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE) {
            mPhotoPath = "file:" + photoFile.absolutePath
            mAbsolutePhotoPath = photoFile.absolutePath
        } else if (requestCode == PHOTO_REQUEST_CODE_COVER) {
            mPhotoPath2 = "file:" + photoFile.absolutePath
            mAbsolutePhotoPath2 = photoFile.absolutePath
        }
        return photoFile
    }

    private fun openGallery(requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.setType("image/*")
        startActivityForResult(galleryIntent, requestCode)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /**
         * SELECCION DE IMAGEN DESDE LA GALERIA
         */
        if (requestCode == GALLERY_REQUEST_CODE_PROFILE && resultCode == Activity.RESULT_OK) {
            try {
                mPhotoFile = null
                if (data != null) {
                    mImageFile = data.data?.let { FileUtil.from(this, it) }
                }
                mCircleImageViewProfile!!.setImageBitmap(BitmapFactory.decodeFile(mImageFile!!.absolutePath))
            } catch (e: Exception) {
                Log.d("ERROR", "Se produjo un error " + e.message)
                Toast.makeText(this, "Se produjo un error " + e.message, Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE_COVER && resultCode == Activity.RESULT_OK) {
            try {
                mPhotoFile2 = null
                if (data != null) {
                    mImageFile2 = data.data?.let { FileUtil.from(this, it) }
                }
                mImageViewCover!!.setImageBitmap(BitmapFactory.decodeFile(mImageFile2!!.absolutePath))
            } catch (e: Exception) {
                Log.d("ERROR", "Se produjo un error " + e.message)
                Toast.makeText(this, "Se produjo un error " + e.message, Toast.LENGTH_LONG).show()
            }
        }
        /**
         * SELECCION DE FOTOGRAFIA
         */
        if (requestCode == PHOTO_REQUEST_CODE_PROFILE && resultCode == Activity.RESULT_OK) {
            mImageFile = null
            mPhotoFile = File(mAbsolutePhotoPath)
            Picasso.with(this@EditProfileActivity).load(mPhotoPath).into(mCircleImageViewProfile)
        }
        /**
         * SELECCION DE FOTOGRAFIA
         */
        if (requestCode == PHOTO_REQUEST_CODE_COVER && resultCode == Activity.RESULT_OK) {
            mImageFile2 = null
            mPhotoFile2 = File(mAbsolutePhotoPath2)
            Picasso.with(this@EditProfileActivity).load(mPhotoPath2).into(mImageViewCover)
        }
    }

    override fun onStart() {
        super.onStart()
        ViewedMessageHelper.updateOnline(true, this@EditProfileActivity)
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@EditProfileActivity)
    }
}