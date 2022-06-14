package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Date(
    var year: Int = 0,
    var mounth: Int = 0,
    var day: Int = 0
) : Parcelable
