package org.kimp.mustep.domain

data class Floor (
    var number: Long,
    var points: List<Point> = ArrayList()
)
