package com.uts.socialuts.providers

import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.uts.socialuts.utils.CompressorBitmapImage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*

class ImageProvider {
    var storage: StorageReference = Firebase.storage.reference

    fun save(context: Context?, file: File): UploadTask {
        val imageByte: ByteArray = CompressorBitmapImage.getImage(context, file.path, 500, 500)
        this.storage = this.storage.child(Date().toString() + ".jpg")
        return storage.putBytes(imageByte)
    }
}