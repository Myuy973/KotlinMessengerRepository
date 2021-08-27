package com.example.kotlinmessenger.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize


@Parcelize
@IgnoreExtraProperties
data class User(
    var uid: String,
    var userName: String,
    var userEmail: String,
    var profileImageUri: String): Parcelable {
    constructor(): this( "",  "", "", "")
}
