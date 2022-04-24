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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.uts.socialuts.R
import com.uts.socialuts.models.Post
import com.uts.socialuts.providers.AuthProvider
import com.uts.socialuts.providers.ImageProvider
import com.uts.socialuts.providers.PostProvider
import com.uts.socialuts.utils.FileUtil
import com.uts.socialuts.utils.ViewedMessageHelper
import de.hdodenhof.circleimageview.CircleImageView
import dmax.dialog.SpotsDialog
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class PostActivity : AppCompatActivity() {
    var mImageViewPost1: ImageView? = null
    var mImageViewPost2: ImageView? = null
    var mImageFile: File? = null
    var mImageFile2: File? = null
    var mButtonPost: Button? = null
    var mImageProvider: ImageProvider? = null
    var mPostProvider: PostProvider? = null
    var mAuthProvider: AuthProvider? = null
    var mTextInputTitle: TextInputEditText? = null
    var mTextInputDescription: TextInputEditText? = null
    var mImageViewAlumno: ImageView? = null
    var mImageViewDocente: ImageView? = null
    var mImageViewDirectivo: ImageView? = null
    var mImageViewOperario: ImageView? = null
    var mCircleImageBack: CircleImageView? = null
    var mTextViewCategory: TextView? = null
    var mCategory = ""
    var mTitle = ""
    var mDescription = ""
    var mDialog: AlertDialog? = null
    var mBuilderSelector: AlertDialog.Builder? = null
    lateinit var options: Array<CharSequence>
    private val GALLERY_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE_2 = 2
    private val PHOTO_REQUEST_CODE = 3
    private val PHOTO_REQUEST_CODE_2 = 4

    // FOTO 1
    var mAbsolutePhotoPath: String? = null
    var mPhotoPath: String? = null
    var mPhotoFile: File? = null

    // FOTO 2
    var mAbsolutePhotoPath2: String? = null
    var mPhotoPath2: String? = null
    var mPhotoFile2: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        mImageProvider = ImageProvider()
        mPostProvider = PostProvider()
        mAuthProvider = AuthProvider()
        mDialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Espere un momento")
            .setCancelable(false).build()
        mBuilderSelector = AlertDialog.Builder(this)
        mBuilderSelector!!.setTitle("Selecciona una opcion")
        options = arrayOf("Imagen de galeria", "Tomar foto")
        mImageViewPost1 = findViewById(R.id.imageViewPost1)
        mImageViewPost2 = findViewById(R.id.imageViewPost2)
        mButtonPost = findViewById(R.id.btnPost)
        mTextInputTitle = findViewById(R.id.textInputVideoGame)
        mTextInputDescription = findViewById(R.id.textInputDescription)
        mImageViewAlumno = findViewById(R.id.imageViewAlumno)
        mImageViewDocente = findViewById(R.id.imageViewDocente)
        mImageViewDirectivo = findViewById(R.id.imageViewDirectivo)
        mImageViewOperario = findViewById(R.id.imageViewOperario)
        mTextViewCategory = findViewById(R.id.textViewCategory)
        mCircleImageBack = findViewById(R.id.circleImageBack)
        mCircleImageBack!!.setOnClickListener { finish() }
        mButtonPost!!.setOnClickListener { clickPost() }
        mImageViewPost1!!.setOnClickListener { selectOptionImage(1) }
        mImageViewPost2!!.setOnClickListener { selectOptionImage(2) }
        mImageViewAlumno!!.setOnClickListener {
            mCategory = "ALUMNO"
            mTextViewCategory?.text = mCategory
        }
        mImageViewDocente!!.setOnClickListener {
            mCategory = "DOCENTE"
            mTextViewCategory?.text = mCategory
        }
        mImageViewDirectivo!!.setOnClickListener {
            mCategory = "DIRECTIVO"
            mTextViewCategory?.text = mCategory
        }
        mImageViewOperario!!.setOnClickListener {
            mCategory = "OPERARIO"
            mTextViewCategory?.text = mCategory
        }
    }

    private fun selectOptionImage(numberImage: Int) {
        mBuilderSelector?.setItems(options) { dialogInterface, i ->
            if (i == 0 && numberImage == 1) {
                openGallery(GALLERY_REQUEST_CODE)
            } else if (i == 0 && numberImage == 2) {
                openGallery(GALLERY_REQUEST_CODE_2)
            } else if (i == 1 && numberImage == 1) {
                takePhoto(PHOTO_REQUEST_CODE)
            } else if (i == 1 && numberImage == 2) {
                takePhoto(PHOTO_REQUEST_CODE_2)
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
                val photoUri: Uri =
                    FileProvider.getUriForFile(this@PostActivity, "com.uts.socialuts", photoFile)
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
        if (requestCode == PHOTO_REQUEST_CODE) {
            mPhotoPath = "file:" + photoFile.absolutePath
            mAbsolutePhotoPath = photoFile.absolutePath
        } else if (requestCode == PHOTO_REQUEST_CODE_2) {
            mPhotoPath2 = "file:" + photoFile.absolutePath
            mAbsolutePhotoPath2 = photoFile.absolutePath
        }
        return photoFile
    }

    private fun clickPost() {
        mTitle = mTextInputTitle?.text.toString()
        mDescription = mTextInputDescription?.text.toString()

        if (mTitle.isEmpty() || mDescription.isEmpty() || mCategory.isEmpty()) {
            Toast.makeText(this, "Completa los campos para publicar", Toast.LENGTH_SHORT).show()
            return
        }
        if ((mImageFile == null && mImageFile2 == null) && (mPhotoFile == null && mPhotoFile2 == null)){
            Toast.makeText(this, "Debes seleccionar una imagen", Toast.LENGTH_SHORT).show()
            return
        }
        // SELECCIONO AMBAS IMAGENES DE LA GALERIA
        if (mImageFile != null && mImageFile2 != null) {
            saveImage(mImageFile!!, mImageFile2!!)
        } else if (mPhotoFile != null && mPhotoFile2 != null) {
            saveImage(mPhotoFile!!, mPhotoFile2!!)
        } else if (mImageFile != null && mPhotoFile2 != null) {
            saveImage(mImageFile!!, mPhotoFile2!!)
        } else if (mPhotoFile != null && mImageFile2 != null) {
            saveImage(mPhotoFile!!, mImageFile2!!)
        }
    }

    private fun saveImage(imageFile1: File, imageFile2: File) {
        mDialog!!.show()
        mImageProvider?.save(this@PostActivity, imageFile1)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mImageProvider?.storage?.downloadUrl
                        ?.addOnSuccessListener { uri ->
                            val url = uri.toString()
                            mImageProvider!!.save(this@PostActivity, imageFile2)
                                .addOnCompleteListener { taskImage2 ->
                                    if (taskImage2.isSuccessful) {
                                        mImageProvider!!.storage.downloadUrl
                                            .addOnSuccessListener { uri2 ->
                                                val url2 = uri2.toString()
                                                val post = Post()
                                                post.image1 = url
                                                post.image2 = url2
                                                post.title = mTitle.lowercase(Locale.getDefault())
                                                post.description = mDescription
                                                post.category = mCategory
                                                post.idUser = mAuthProvider?.getUid()
                                                post.timestamp = Date().time
                                                mPostProvider?.save(post)
                                                    ?.addOnCompleteListener { taskSave ->
                                                        mDialog!!.dismiss()
                                                        if (taskSave.isSuccessful) {
                                                            clearForm()
                                                            Toast.makeText(
                                                                this@PostActivity,
                                                                "La informacion se almaceno correctamente",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                this@PostActivity,
                                                                "No se pudo almacenar la informacion",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                            }
                                    } else {
                                        mDialog!!.dismiss()
                                        Toast.makeText(
                                            this@PostActivity,
                                            "La imagen numero 2 no se pudo guardar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }.addOnFailureListener {
                                    it.message?.let { it1 -> Log.d("ERROR", it1) }
                                }
                        }?.addOnFailureListener {
                            it.message?.let { it1 -> Log.d("ERROR", it1) }
                        }
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(
                        this@PostActivity,
                        "Hubo error al almacenar la imagen",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun clearForm() {
        mTextInputTitle?.setText("")
        mTextInputDescription?.setText("")
        mTextViewCategory?.text = "CATEGORIAS"
        mImageViewPost1!!.setImageResource(R.drawable.upload_image)
        mImageViewPost2!!.setImageResource(R.drawable.upload_image)
        mTitle = ""
        mDescription = ""
        mCategory = ""
        mImageFile = null
        mImageFile2 = null
    }

    private fun openGallery(requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /**
         * SELECCION DE IMAGEN DESDE LA GALERIA
         */
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                mPhotoFile = null
                if (data != null) {
                    mImageFile = data.data?.let { FileUtil.from(this, it) }
                }
                mImageViewPost1!!.setImageBitmap(BitmapFactory.decodeFile(mImageFile!!.absolutePath))
            } catch (e: Exception) {
                Log.d("ERROR", "Se produjo un error " + e.message)
                Toast.makeText(this, "Se produjo un error " + e.message, Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE_2 && resultCode == Activity.RESULT_OK) {
            try {
                mPhotoFile2 = null
                if (data != null) {
                    mImageFile2 = data.data?.let { FileUtil.from(this, it) }
                }
                mImageViewPost2!!.setImageBitmap(BitmapFactory.decodeFile(mImageFile2!!.absolutePath))
            } catch (e: Exception) {
                Log.d("ERROR", "Se produjo un error " + e.message)
                Toast.makeText(this, "Se produjo un error " + e.message, Toast.LENGTH_LONG).show()
            }
        }
        /**
         * SELECCION DE FOTOGRAFIA
         */
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mImageFile = null
            mPhotoFile = File(mAbsolutePhotoPath)
            Picasso.with(this@PostActivity).load(mPhotoPath).into(mImageViewPost1)
        }
        /**
         * SELECCION DE FOTOGRAFIA
         */
        if (requestCode == PHOTO_REQUEST_CODE_2 && resultCode == Activity.RESULT_OK) {
            mImageFile2 = null
            mPhotoFile2 = File(mAbsolutePhotoPath2)
            Picasso.with(this@PostActivity).load(mPhotoPath2).into(mImageViewPost2)
        }
    }

    override fun onStart() {
        super.onStart()
        ViewedMessageHelper.updateOnline(true, this@PostActivity)
    }

    protected override fun onPause() {
        super.onPause()
        ViewedMessageHelper.updateOnline(false, this@PostActivity)
    }
}