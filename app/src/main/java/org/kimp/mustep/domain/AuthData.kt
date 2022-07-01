package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthData(
    val name: String = "",
    val mail: String = "",
    val pass: String = ""
) : Parcelable
