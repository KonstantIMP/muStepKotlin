package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class University(
    var uid: String = "",
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var floors: Long = 0,
    var name: TranslatableEntry = TranslatableEntry(),
    var address: TranslatableEntry = TranslatableEntry(),
    var official: Boolean = false
) : Parcelable
