package org.kimp.mustep.utils

import java.text.SimpleDateFormat
import java.util.Locale
import org.kimp.mustep.domain.Date
import org.kimp.mustep.domain.Time

object DateFormatter {
    fun toAndroidDate(date: Date, time: Time) : java.util.Date {
        val fmt = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        return fmt.parse(
            String.format(
                "%02d/%02d/%d %02d:%02d",
                    date.mounth, date.day, date.year,
                    time.hours, time.minutes
                )
        )!!
    }
}