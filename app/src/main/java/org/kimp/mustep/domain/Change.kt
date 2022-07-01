package org.kimp.mustep.domain

import android.graphics.Bitmap

data class Change(
    val desc: TranslatableEntry,
    val image: String = "",
    var imageBitmap: Bitmap? = null
)
