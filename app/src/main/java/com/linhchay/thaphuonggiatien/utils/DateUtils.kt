package com.linhchay.thaphuonggiatien.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DateUtils {

    fun getRemainingDaysDescription(eventDateMillis: Long): String {
        if (eventDateMillis == 0L) return ""

        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        val eventDate = Calendar.getInstance()
        eventDate.timeInMillis = eventDateMillis
        eventDate.set(Calendar.HOUR_OF_DAY, 0)
        eventDate.set(Calendar.MINUTE, 0)
        eventDate.set(Calendar.SECOND, 0)
        eventDate.set(Calendar.MILLISECOND, 0)

        // Set event year to current year
        eventDate.set(Calendar.YEAR, now.get(Calendar.YEAR))

        // If event date has passed this year, set it to next year
        if (eventDate.before(now)) {
            eventDate.add(Calendar.YEAR, 1)
        }

        val diffInMillis = eventDate.timeInMillis - now.timeInMillis
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            diffInDays == 0L -> "Hôm nay"
            diffInDays == 1L -> "Còn 1 ngày"
            else -> "Còn $diffInDays ngày"
        }
    }
}
