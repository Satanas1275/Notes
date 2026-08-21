package com.satanas1275.notes.ui.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH)
private val FULL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

fun formatNoteDate(time: Long): String {
    val now = LocalDate.now()
    val dateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    return when {
        date == now -> TIME_FORMAT.format(dateTime)
        date == now.minusDays(1) -> "Hier"
        date.year == now.year -> DAY_FORMAT.format(date)
        else -> FULL_FORMAT.format(date)
    }
}
