package com.satanas1275.notes.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.satanas1275.notes.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun formatNoteDate(time: Long): String {
    // Suit la langue courante de l'app (au lieu d'être figé en français) :
    // les mois affichés changent bien si l'utilisateur choisit une autre
    // langue dans les réglages.
    val locale = LocalConfiguration.current.locales[0]
    val now = LocalDate.now()
    val dateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    return when {
        date == now -> TIME_FORMAT.format(dateTime)
        date == now.minusDays(1) -> stringResource(R.string.yesterday)
        date.year == now.year -> DateTimeFormatter.ofPattern("d MMMM", locale).format(date)
        else -> DateTimeFormatter.ofPattern("d MMMM yyyy", locale).format(date)
    }
}
