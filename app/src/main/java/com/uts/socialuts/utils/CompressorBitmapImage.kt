package com.uts.socialuts.utils


import android.graphics.Bitmap
import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


/**
 * Created by Optic on 24/01/2018.
 */
object CompressorBitmapImage {
    /*
     * Metodo que permite comprimir imagenes y transformarlas a bitmap
     */
    fun getImage(
        ctx: Context?,
        path: String?,
        width: Int,
        height: Int
    ): ByteArray {
        val file_thumb_path= File(path)
        var thumb_bitmap: Bitmap? = null

        try {
            thumb_bitmap = id.zelory.compressor.Compressor(ctx)
                .setMaxWidth(width)
                .setMaxHeight(height)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .compressToBitmap(file_thumb_path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val baos = ByteArrayOutputStream()
        thumb_bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return baos.toByteArray()
    }
}