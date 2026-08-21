package com.satanas.notes.ui.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatNoteDate(time: Long): String {
    val now = LocalDate.now()
    val dateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    return when {
        date == now -> DateTimeFormatter.ofPattern("HH:mm").format(dateTime)
        date == now.minusDays(1) -> "Hier"
        date.year == now.year -> DateTimeFormatter.ofPattern("d MMMM", Locale.FRENCH).format(date)
        else -> DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH).format(date)
    }
}
