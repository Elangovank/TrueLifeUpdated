package com.truelife.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.APP_DIR
import com.truelife.app.constants.TLConstant.COMPRESSED_IMAGES_DIR
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_60
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_70
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_80
import id.zelory.compressor.Compressor
import java.io.File

object FileCompression {

    fun getBitmapByUri(context: Context, uri: Uri): Bitmap? {
        return try {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(context.contentResolver, uri)
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
    fun compressImage(mContext: Context, mCompressFile: File): File {
        try2CreateCompressDir()
        var quality = 75
        val outPath: String = (Environment.DIRECTORY_DOWNLOADS
            .toString() + File.separator
                + APP_DIR
                + COMPRESSED_IMAGES_DIR)
        val size = Utility.getFileSize(mCompressFile)
        if (size > TLConstant.IMAGE_FILE_SIZE_LIMIT_8MB) {
            quality = IMAGE_QUALITY_60
        } else if (size > TLConstant.IMAGE_FILE_SIZE_LIMIT_5MB) {
            quality = IMAGE_QUALITY_70
        } else if (size > TLConstant.IMAGE_FILE_SIZE_LIMIT_3MB) {

            quality = IMAGE_QUALITY_80
        } else {
            quality = 75
        }
        return Compressor(mContext)
            .setQuality(quality)
           // .setCompressFormat(Bitmap.CompressFormat.WEBP)
            .setDestinationDirectoryPath(outPath)
            .compressToFile(mCompressFile)

    }

    fun try2CreateCompressDir() {
        var f = File(
            Environment.DIRECTORY_DOWNLOADS,
            File.separator + TLConstant.APP_DIR
        )
        f.mkdirs()
        f = File(
            Environment.getExternalStorageDirectory(),
            File.separator + TLConstant.APP_DIR + TLConstant.COMPRESSED_IMAGES_DIR
        )
        f.mkdirs()
        f = File(
            Environment.getExternalStorageDirectory(),
            File.separator + TLConstant.APP_DIR + TLConstant.TEMP_DIR
        )
        f.mkdirs()
    }
}