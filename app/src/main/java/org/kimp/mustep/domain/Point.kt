package org.kimp.mustep.domain

data class Point(
    var uid: String = "",
    var number: Long = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var name: TranslatableEntry = TranslatableEntry(),
    var data: TranslatableEntry = TranslatableEntry()
)
