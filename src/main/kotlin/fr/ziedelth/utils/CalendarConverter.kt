package fr.ziedelth.utils

import java.text.SimpleDateFormat
import java.util.*

val DATE_FORMAT_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z\$".toRegex()
fun Calendar.toISO8601(): String = CalendarConverter.fromUTCTimestampString(this)

object CalendarConverter {
    private val utcFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val timeZone: TimeZone = TimeZone.getTimeZone("UTC")

    fun fromUTCTimestampString(iso8601calendar: Calendar?): String {
        this.utcFormatter.timeZone = timeZone
        return this.utcFormatter.format(Date.from(iso8601calendar?.toInstant()))
    }

    fun toUTCCalendar(iso8601String: String): Calendar {
        this.utcFormatter.timeZone = timeZone
        val calendar = Calendar.getInstance()
        val date = this.utcFormatter.parse(iso8601String)
        calendar.time = date
        calendar.timeZone = timeZone
        return calendar
    }
}
