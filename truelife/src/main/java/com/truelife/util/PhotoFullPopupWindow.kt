/*******************************************************************************
 * Copyright 2016 stfalcon.com
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.truelife.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.truelife.R
import com.truelife.util.Utility.loadImage

class PhotoFullPopupWindow(
    var mContext: FragmentActivity,
    layout: Int,
    v: View?,
    imageUrl: String?,
    bitmap: Bitmap?
) : PopupWindow(
    (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
        R.layout.popup_photo_full,
        null
    ), ViewGroup.LayoutParams.MATCH_PARENT,
    ViewGroup.LayoutParams.MATCH_PARENT
) {
    private val myImageView: ImageView
    var view: View
    var loading: ProgressBar
    var parent: ViewGroup? = null

    companion object {
        private val instance: PhotoFullPopupWindow? = null
    }

    init {
        view = contentView
        val closeButton = view.findViewById<TextView>(R.id.ib_close)
        isOutsideTouchable = true
        isFocusable = true
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener {
            // Dismiss the popup window
            dismiss()
        }
        //---------Begin customising this popup--------------------
        loading = view.findViewById(R.id.loading)
        myImageView = view.findViewById(R.id.image)
        // ImageUtils.setZoomable(imageView);
//----------------------------
        if (bitmap != null) {
            loading.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= 16) {
            } else {
            }
        } else {
            loading.isIndeterminate = true
            loading.visibility = View.GONE
            loadImage(imageUrl!!, myImageView)
            showAtLocation(v, Gravity.CENTER, 0, 0)
        }
        //------------------------------
    }
}