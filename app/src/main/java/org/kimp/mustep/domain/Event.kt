package org.kimp.mustep.domain

data class Event (
    var uid: String = "",
    var university: String = "",
    var date: Date = Date(),
    var time: Time = Time(),
    var name: TranslatableEntry = TranslatableEntry(),
    var guide: TranslatableEntry = TranslatableEntry(),
    var languages: TranslatableEntry = TranslatableEntry(),
    var guide_avatar: String = "",
    var users_count: Int = 0,
    var users: List<String> = ArrayList()
)