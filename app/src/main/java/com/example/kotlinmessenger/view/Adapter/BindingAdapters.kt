package com.example.kotlinmessenger.view.Adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmessenger.R
import de.hdodenhof.circleimageview.CircleImageView

object BindingAdapters {

    // bitmap設定後Viewへ画像表示
    @BindingAdapter("bitmap")
    @JvmStatic
    fun imageBitmapSet(view: CircleImageView, bitmap: Bitmap?) {
        if (bitmap != null) {
            view.setImageBitmap(bitmap)
        }
    }

    // ChatLogでメッセージを追加したら最新メッセージへ自動スクロール
    @BindingAdapter("chatPageScroll")
    @JvmStatic
    fun scroll(view: RecyclerView, position: Int) {
        view.scrollToPosition(position)
    }

}