package uz.relay.todoapp.utils

import uz.relay.todoapp.domain.model.Priority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

data class ParsedTask(
    val title: String,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val priority: Priority = Priority.NONE,
    val listName: String? = null
)

/**
 * Reads a one-line task such as "Call mom tomorrow 18:00 !high #Home".
 * Words it understands are removed from the title; everything else stays.
 */
object QuickAddParser {

    private val timeRegex = Regex("""^(\d{1,2})(?::(\d{2}))?(am|pm)?$""", RegexOption.IGNORE_CASE)
    private val days = mapOf(
        "mon" to DayOfWeek.MONDAY, "monday" to DayOfWeek.MONDAY,
        "tue" to DayOfWeek.TUESDAY, "tuesday" to DayOfWeek.TUESDAY,
        "wed" to DayOfWeek.WEDNESDAY, "wednesday" to DayOfWeek.WEDNESDAY,
        "thu" to DayOfWeek.THURSDAY, "thursday" to DayOfWeek.THURSDAY,
        "fri" to DayOfWeek.FRIDAY, "friday" to DayOfWeek.FRIDAY,
        "sat" to DayOfWeek.SATURDAY, "saturday" to DayOfWeek.SATURDAY,
        "sun" to DayOfWeek.SUNDAY, "sunday" to DayOfWeek.SUNDAY
    )

    fun parse(text: String, today: LocalDate = LocalDate.now()): ParsedTask {
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val title = mutableListOf<String>()
        var date: LocalDate? = null
        var time: LocalTime? = null
        var priority = Priority.NONE
        var listName: String? = null

        var i = 0
        while (i < words.size) {
            val word = words[i]
            val lower = word.lowercase()
            val nextLower = words.getOrNull(i + 1)?.lowercase()

            when {
                lower == "today" || lower == "tod" -> date = today
                lower == "tonight" -> { date = today; if (time == null) time = LocalTime.of(20, 0) }
                lower == "tomorrow" || lower == "tmr" || lower == "tom" -> date = today.plusDays(1)
                lower == "next" && nextLower == "week" -> {
                    date = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); i++
                }
                lower in days -> date = today.with(TemporalAdjusters.nextOrSame(days.getValue(lower)))
                lower == "at" && nextLower != null && parseTime(nextLower, requireMarker = false) != null -> {
                    time = parseTime(nextLower, requireMarker = false); i++
                }
                parseTime(lower, requireMarker = true) != null -> time = parseTime(lower, requireMarker = true)
                lower in setOf("!high", "!h", "!!!", "!3") -> priority = Priority.HIGH
                lower in setOf("!medium", "!med", "!m", "!!", "!2") -> priority = Priority.MEDIUM
                lower in setOf("!low", "!l", "!", "!1") -> priority = Priority.LOW
                word.length > 1 && word.startsWith("#") -> listName = word.drop(1)
                else -> title += word
            }
            i++
        }

        if (time != null && date == null) date = today
        val cleanTitle = title.joinToString(" ").ifBlank { text.trim() }
        return ParsedTask(cleanTitle, date, time, priority, listName)
    }

    /** "18:00", "6pm", "6:30am"; a bare "18" only after "at". */
    private fun parseTime(token: String, requireMarker: Boolean): LocalTime? {
        val match = timeRegex.matchEntire(token) ?: return null
        val (h, m, ampm) = match.destructured
        if (requireMarker && m.isEmpty() && ampm.isEmpty()) return null
        var hour = h.toInt()
        val minute = m.ifEmpty { "0" }.toInt()
        when (ampm.lowercase()) {
            "pm" -> if (hour < 12) hour += 12
            "am" -> if (hour == 12) hour = 0
        }
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }
}
