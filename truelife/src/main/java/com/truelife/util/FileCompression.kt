package com.truelife.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.APP_DIR
import com.truelife.app.constants.TLConstant.COMPRESSED_IMAGES_DIR
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_60
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_70
import com.truelife.app.constants.TLConstant.IMAGE_QUALITY_80
import id.zelory.compressor.Compressor
import java.io.File

object FileCompression {


    fun compressImage(mContext: Context, mCompressFile: File): File {
        try2CreateCompressDir()
        var quality = 75
        val outPath: String = (Environment.getExternalStorageDirectory()
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
            Environment.getExternalStorageDirectory(),
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