package com.truelife.chat.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore

object MediaStoreUtil {
    fun getBitmapByUri(context: Context, uri: Uri): Bitmap? {
        return try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source: ImageDecoder.Source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                bitmap
            } else {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                bitmap
            }
        } catch (e: Exception) {
            null
        }


    }
}