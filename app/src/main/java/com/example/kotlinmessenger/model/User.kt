package com.example.kotlinmessenger.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize


@Parcelize
@IgnoreExtraProperties
data class User(val uid: String, val username: String, val profileImageUri: String): Parcelable {
    constructor(): this( "",  "",  "")
}
