package com.example.kotlinmessenger.view.Adapter

import android.graphics.Bitmap
import android.view.View
import androidx.databinding.BindingAdapter
import com.example.kotlinmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

object BindingAdapters {

    @BindingAdapter("bitmap")
    @JvmStatic
    fun imageBitmapSet(view: CircleImageView, bitmap: Bitmap?) {
        view.setImageBitmap(bitmap)
    }

}