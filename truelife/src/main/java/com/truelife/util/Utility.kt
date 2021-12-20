package com.truelife.util

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.truelife.R
import com.truelife.app.activity.ProfileActivity
import com.truelife.app.constants.TLConstant
import com.truelife.app.constants.TLConstant.MALE
import com.truelife.app.listeners.ImageLoaderCallback
import com.truelife.app.touchimageview.TLMediaPreviewActivity
import org.apache.commons.lang3.StringEscapeUtils
import java.io.*
import java.text.DecimalFormat


/**
 * Created by Elango on 31/01/19.
 **/

object Utility {
    private val df = DecimalFormat("#.###")
    private val dfSingle = DecimalFormat("#.#")
    private val dfMoney = DecimalFormat("#.00")

    fun formatDecimal(value: Double): String {
        return df.format(value)
    }

    fun formatDecimalToSingleDigit(value: Double): String {
        return dfSingle.format(value)
    }

    fun formatDecimalToMoney(value: Double): String {
        return dfMoney.format(value)
    }

    @SuppressLint("MissingPermission")
    fun isInternetAvailable(context: Context?): Boolean {
        try {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo: NetworkInfo?
            netInfo = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected) {
                return true
            } else {
                AppDialogs.customOkAction(
                    context,
                    null,
                    context.getString(R.string.no_internet),
                    null,
                    null,
                    false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return false
    }

    fun isInternetAvailable(context: Context?, isShowAlert: Boolean): Boolean {
        try {
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo: NetworkInfo?
            netInfo = cm.activeNetworkInfo
            if (netInfo != null && netInfo.isConnected) {
                return true
            } else {
                if (isShowAlert) {
                    AppDialogs.customOkAction(
                        context,
                        null,
                        context.getString(R.string.no_internet),
                        null,
                        null,
                        false
                    )
                } else {
                    AppDialogs.showToastDialog(context, context.getString(R.string.no_internet))
                }
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Get color value according to the version compat
     *
     * @param c     Context
     * @param color color resource int sample R.color.accent
     * @return Parsed int value equal to [android.graphics.Color] values
     */
    fun getColor(c: Context, color: Int): Int {
        return ContextCompat.getColor(c, color)
    }

    /**
     * Get color value according to the version compat
     *
     * @param c  Context
     * @param id drawable resource int sample R.drawable.rounded_bg
     * @return Parsed int value equal to [android.graphics.Color] values
     */
    fun getDrawable(c: Context, id: Int): Drawable {
        return ContextCompat.getDrawable(c, id)!!
    }

    fun errorText(text: String): String? {
        return String.format("%s should not be empty", text)

    }

    fun log(msg: String) {
        Log.d("FFFFFFFF ---> ", msg)
    }

    fun get_roundImage(c: Context, bitmap: Bitmap): Bitmap? {
        try {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            val paint = Paint()
            paint.isAntiAlias = true

            val canvas = Canvas(output)
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(
                (bitmap.width / 2).toFloat(),
                (bitmap.height / 2).toFloat(),
                (bitmap.width / 2).toFloat(),
                paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            canvas.drawBitmap(bitmap, rect, rect, paint)

            return output

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun encodeFileToBase64Binary(filePath: String): String? {
        var encodedfile: String? = null
        try {
            val file = File(filePath)
            val fileInputStreamReader = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            fileInputStreamReader.read(bytes)
            encodedfile = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return encodedfile
    }

    fun decodeFileToBase64Binary(data: String): Bitmap? {
        val decodedString = Base64.decode(data, Base64.DEFAULT)
        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        return decodedByte
    }

    fun colorString(text: String): String? {
        return String.format("<b>%s</b>", text)
    }

    fun noColorString(text: String): String? {
        return String.format("%s", text)
    }

    fun getFirstLetterCaps(text: String): String? {
        try {
            val output = String.format("%s", text.substring(0, 1).toUpperCase() + text.substring(1))
            return output
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun stripNonDigits(input: CharSequence): String {
        val sb = StringBuilder(input.length)
        (0 until input.length).map { input[it] }.filter { it.toInt() in 48..57 }
            .forEach { sb.append(it) }
        return sb.toString()
    }

    /**
     * Load images
     */
    fun loadImage(aURL: String, image: ImageView) {
        try {

            if (aURL.isEmpty())
                return

            Picasso.get().load(aURL)
                .placeholder(R.drawable.ic_male)
                .error(R.drawable.ic_male)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun loadImageWithCallback(aURL: String, image: ImageView , listener : ImageLoaderCallback) {
        try {

            if (aURL.isEmpty())
                return

            Picasso.get().load(aURL)
                .placeholder(R.drawable.ic_male)
                .error(R.drawable.ic_male)
                .into(image)
            listener.load()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Load images
     */
    fun loadImageNotification(aURL: String, image: ImageView) {
        try {

            if (aURL.isEmpty())
                return

            Picasso.get().load(aURL)
                .placeholder(R.drawable.notification_placeholder)
                .error(R.drawable.notification_placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun loadImageWithGender(aURL: String, image: ImageView, gender: String) {
        try {

            if (aURL.isEmpty())
                return

            Picasso.get().load(aURL)
                .placeholder(
                    if (gender == MALE) {
                        R.drawable.ic_male
                    } else R.drawable.ic_female
                )
                .error(
                    if (gender == MALE) {
                        R.drawable.ic_male
                    } else R.drawable.ic_female
                )
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImage(aURL: String, image: ImageView, placeholder: Int) {
        try {

            if (aURL.isEmpty())
                return

            Picasso.get().load(aURL)
                .placeholder(placeholder)
                .error(placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImage(file: Int, image: ImageView) {
        try {
            Picasso.get().load(file)
                .placeholder(R.drawable.ic_user_placeholder)
                .error(R.drawable.ic_user_placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImageWithoutplaceholder(aURL: String, image: ImageView) {
        try {
            Picasso.get().load(aURL)
                .error(R.drawable.ic_user_placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun loadImageWithClubplaceholder(aURL: String, image: com.truelife.util.TLCircularImageView) {
        try {
            Picasso.get().load(aURL)
                .error(R.drawable.club_placeholder)
                .placeholder(R.drawable.club_placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadImageWithClubplaceholderImageView(aURL: String, image: ImageView) {
        try {
            Picasso.get().load(aURL)
                .error(R.drawable.club_placeholder)
                .placeholder(R.drawable.club_placeholder)
                .into(image)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun loadPlaceHolder(c: Context, gender: String, image: ImageView) {
        try {

            if (gender.isEmpty()) {
                image.setImageDrawable(getDrawable(c, R.drawable.ic_user_placeholder))
                return
            }

            if (gender == MALE)
                image.setImageDrawable(getDrawable(c, R.drawable.ic_male))
            else image.setImageDrawable(getDrawable(c, R.drawable.ic_female))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("DefaultLocale")
    fun getMediaType(aURL: String): String? {
        var aMediaType = ""

        if (aURL.isEmpty())
            return aMediaType

        val type = aURL.substring(aURL.lastIndexOf(".")).toLowerCase()
        aMediaType =
            if (type == ".docx" || type == ".csv" || type == ".txt" || type == ".pdf") {
                "document"
            } else if (type == ".mp3" || type == ".wav" || type == ".adts") {
                "audio"
            } else if (type == ".jpeg" || type == ".jpg" || type == ".png") {
                "image"
            } else
                "video"

        return aMediaType
    }

    /**
     * Show Date Picker
     */

    fun showDatePicker(context: Context, view: TextView, format: String, hidePast: Boolean) {
        val datePicker = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
            val date = String.format(
                "%s-%s-%s",
                datePicker.year,
                datePicker.month + 1,
                datePicker.dayOfMonth
            )
            view.text = DateUtil.convertDateFormat(date, TLConstant.YY_MM_DD, format)
        }

        if (view.text.isEmpty())
            DateUtil.showDateDialog(context, datePicker, hidePast)
        else DateUtil.showFromDateDialog(
            context,
            DateUtil.convertDateFormat(
                view.text.toString(),
                format,
                TLConstant.DD_MM_YY
            ), datePicker, hidePast
        )
    }


    /**
     * Show Time Picker
     */

    fun showTimePicker(context: Context, view: TextView, isCancelable: Boolean) {
        val array = DateUtil.getTimeSlot(context, TLConstant.HH_MM_AA, 30)
        AppDialogs.showSingleChoice(
            context,
            context.getString(R.string.choose_time_caps),
            array,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    view.text = text
                }
            },
            isCancelable
        )
    }

    /**
     * Show Time Picker
     */

    fun showTimePicker(
        context: Context,
        view: TextView,
        array: ArrayList<String>,
        isCancelable: Boolean
    ) {
        AppDialogs.showSingleChoice(
            context,
            context.getString(R.string.choose_time_caps),
            array,
            object : SingleChoiceAdapter.Callback {
                override fun info(position: Int, text: String) {
                    AppDialogs.hideSingleChoice()
                    view.text = text
                }
            },
            isCancelable
        )
    }

    fun findMediaType(aMediaURL: String): String? {
        var aMediaType = ""
        aMediaType =
            if (aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".docx" || aMediaURL.substring(
                    aMediaURL.lastIndexOf(".")
                ).toLowerCase() == ".csv" || aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".txt" || aMediaURL.substring(
                    aMediaURL.lastIndexOf(".")
                ).toLowerCase() == ".pdf"
            ) {
                "document"
            } else if (aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".mp3" || aMediaURL.substring(
                    aMediaURL.lastIndexOf(".")
                ).toLowerCase() == ".wav" || aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".adts"
            ) {
                "audio"
            } else if (aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".jpeg" || aMediaURL.substring(
                    aMediaURL.lastIndexOf(".")
                ).toLowerCase() == ".jpg" || aMediaURL.substring(aMediaURL.lastIndexOf("."))
                    .toLowerCase() == ".png"
            ) {
                "image"
            } else {
                "video"
            }
        return aMediaType
    }

    // Custom method for converting DP/DIP value to pixels
    fun getPixelsFromDPs(context: Context, dps: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), r.displayMetrics
        ).toInt()
    }

    fun rotateImage(aFile: File): Bitmap? {
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        BitmapFactory.decodeFile(aFile.toString(), bounds)
        val opts = BitmapFactory.Options()
        val bm = BitmapFactory.decodeFile(aFile.toString(), opts)
        var exif: ExifInterface? = null
        var rotationAngle = 0
        try {
            exif = ExifInterface(aFile.toString())
            val orientString =
                exif.getAttribute(ExifInterface.TAG_ORIENTATION)
            val orientation =
                orientString?.toInt() ?: ExifInterface.ORIENTATION_NORMAL
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle =
                180
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle =
                270
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val matrix = Matrix()
        matrix.setRotate(
            rotationAngle.toFloat(),
            bm.width.toFloat() / 2,
            bm.height.toFloat() / 2
        )
        return Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true)
    }


    fun getFileSize(selectedPath: String?): Int {
        var aSize = 0
        val file = File(selectedPath)
        aSize = (file.length() / 1024).toString().toInt()
        return aSize * 1000
    }

    fun getFileSize(file: File?): Int {
        var aSize = 0
        aSize = (file!!.length() / 1024).toString().toInt()
        return aSize
    }

    /*
    * Encode Emoji Smiley symbol and text
    * */
    fun EncodeEmojiandText(aText: String?): String? {
        return StringEscapeUtils.escapeJava(aText).replace("\\n", "\n")
    }

    fun setFontSemiBold(myContext: Context, myHeaderTXT: TextView) {
        try {
            val type = Typeface.createFromAsset(
                myContext.assets,
                myContext.getString(R.string.opensans_semi_bold)
            )
            myHeaderTXT.setTypeface(type)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalBitmapUri(bmp: Bitmap, mCOn: Context): File {
        val outPath: String = (Environment.getExternalStorageDirectory()
            .toString() + File.separator
                + TLConstant.APP_DIR
                + TLConstant.COMPRESSED_IMAGES_DIR)
        val dir = File(outPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "shared_image" + System.currentTimeMillis() + ".png")
        val fOut = FileOutputStream(file)

        bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()
        return  file
    }

    fun navigateImageView(
        mContext: Context,
        mMediaList: ArrayList<String>,
        mMediaType: ArrayList<String>,
        position: Int
    ) {
        if (isInternetAvailable(mContext)) {
            val intent = Intent(mContext, TLMediaPreviewActivity::class.java)
            val aBundle = Bundle()
            val gson = Gson()
            val json = gson.toJson(mMediaList)
            aBundle.putString("feed_images", json)
            aBundle.putString("feed_images_Type", gson.toJson(mMediaType))
            aBundle.putInt("Position", position)
            intent.putExtra("bundle", aBundle)
            mContext.startActivity(intent)
        }
    }

    fun navigateUserProfile(
        mContext: Context, userId: String
    ) {
        if (isInternetAvailable(mContext)) {
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("userid", userId)
            mContext.startActivity(intent)
        }
    }

}

