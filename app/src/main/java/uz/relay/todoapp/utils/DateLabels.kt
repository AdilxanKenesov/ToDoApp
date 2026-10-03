package uz.relay.todoapp.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
private val weekdayFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
private val fullFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val headerFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
private val monthFormat = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)

fun LocalTime.label(): String = format(timeFormat)

/** "Today", "Tomorrow", "Yesterday", "Mon, 6 Oct" or "6 Oct 2025". */
fun LocalDate.label(today: LocalDate = LocalDate.now()): String = when (this) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    today.minusDays(1) -> "Yesterday"
    else -> if (year == today.year) format(weekdayFormat) else format(fullFormat)
}

fun LocalDate.headerLabel(): String = format(headerFormat)
fun LocalDate.monthLabel(): String = format(monthFormat)

fun LocalDate.shortDay(): String = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)

/** "Today 18:30", "Mon, 6 Oct 09:00". */
fun Long.reminderLabel(today: LocalDate = LocalDate.now()): String {
    val at = toLocalDateTime()
    return "${at.toLocalDate().label(today)} ${at.toLocalTime().label()}"
}
