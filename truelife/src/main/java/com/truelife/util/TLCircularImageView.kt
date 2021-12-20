package com.truelife.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView

class TLCircularImageView : ImageView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
    }

    override fun onDraw(canvas: Canvas) {
        try {
            if (drawable != null) {
                val drawable = drawable as BitmapDrawable ?: return
                if (width == 0 || height == 0) {
                    return
                }
                val fullSizeBitmap = drawable.bitmap
                val scaledWidth = measuredWidth
                val scaledHeight = measuredHeight
                val mScaledBitmap: Bitmap
                mScaledBitmap = if (scaledWidth == fullSizeBitmap.width
                    && scaledHeight == fullSizeBitmap.height
                ) {
                    fullSizeBitmap
                } else {
                    Bitmap.createScaledBitmap(
                        fullSizeBitmap,
                        scaledWidth, scaledHeight, true /* filter */
                    )
                }

                val circleBitmap = getCircledBitmap(mScaledBitmap)
                canvas.drawBitmap(circleBitmap!!, 0f, 0f, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRoundedCornerBitmap(
        context: Context, input: Bitmap?,
        pixels: Int, w: Int, h: Int, squareTL: Boolean, squareTR: Boolean,
        squareBL: Boolean, squareBR: Boolean
    ): Bitmap {
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val densityMultiplier = context.resources
            .displayMetrics.density
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, w, h)
        val rectF = RectF(rect)
        // make sure that our rounded corner is scaled appropriately
        val roundPx = pixels * densityMultiplier
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        // draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0f, 0f, w / 2.toFloat(), h / 2.toFloat(), paint)
        }
        if (squareTR) {
            canvas.drawRect(w / 2.toFloat(), 0f, w.toFloat(), h / 2.toFloat(), paint)
        }
        if (squareBL) {
            canvas.drawRect(0f, h / 2.toFloat(), w / 2.toFloat(), h.toFloat(), paint)
        }
        if (squareBR) {
            canvas.drawRect(w / 2.toFloat(), h / 2.toFloat(), w.toFloat(), h.toFloat(), paint)
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(input!!, 0f, 0f, paint)
        return output
    }

    fun getCircledBitmap(bitmap: Bitmap): Bitmap? {
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(
                bitmap.width, bitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(result)
            val color = Color.BLUE
            val paint = Paint()
            val rect =
                Rect(0, 0, bitmap.width, bitmap.height)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle(
                bitmap.width / 2.toFloat(), bitmap.height / 2.toFloat(),
                bitmap.height / 2.toFloat(), paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}