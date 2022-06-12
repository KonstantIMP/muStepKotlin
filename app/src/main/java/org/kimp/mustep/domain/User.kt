package org.kimp.mustep.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var uid: String = "",
    var name: String = "",
    var surname: String = "",
    var email: String = "",
    var avatar: String = ""
) : Parcelable
