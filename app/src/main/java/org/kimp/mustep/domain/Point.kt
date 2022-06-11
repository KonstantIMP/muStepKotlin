package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Point(
    var uid: String = "",
    var number: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var name: TranslatableEntry = TranslatableEntry(),
    var data: TranslatableEntry = TranslatableEntry()
) : Parcelable
