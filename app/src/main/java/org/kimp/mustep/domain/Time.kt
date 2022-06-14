package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Time(
    var hours: Int = 0,
    var minutes: Int = 0
) : Parcelable
