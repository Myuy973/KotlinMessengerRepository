package com.example.kotlinmessenger.view.Adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

object BindingAdapters {

    @BindingAdapter("bitmap")
    @JvmStatic
    fun imageBitmapSet(view: CircleImageView, bitmap: Bitmap?) {
        Log.d("log", "imageBitmapSet start")
        if (bitmap != null) {
            view.setImageBitmap(bitmap)
        }
    }

    @BindingAdapter("chatPageScroll")
    @JvmStatic
    fun scroll(view: RecyclerView, position: Int) {
        view.scrollToPosition(position)
    }

}