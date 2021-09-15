package com.example.vehiclessale.model

import android.os.Parcelable
import com.google.android.datatransport.cct.StringMerger
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val address: String,
    val email: String,
    val id: String,
    val name: String,
    val pass: String,
    val phone: String
) : Parcelable {
    constructor() : this(
        "", "", "", "", "", ""
    )
}