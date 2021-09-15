package com.example.kotlinmessenger.view.adapter

import android.graphics.Bitmap
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
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