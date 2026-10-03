package uz.relay.todoapp.utils

import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Task
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Clock time for reminder presets such as "Tomorrow" when the task has no due time. */
val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(9, 0)

/** The next date after [from] for this rule; null for one-time tasks. */
fun RepeatRule.nextDate(from: LocalDate): LocalDate? = when (this) {
    RepeatRule.NONE -> null
    RepeatRule.DAILY -> from.plusDays(1)
    RepeatRule.WEEKLY -> from.plusWeeks(1)
    RepeatRule.MONTHLY -> from.plusMonths(1)
    RepeatRule.WEEKDAYS -> {
        var next = from.plusDays(1)
        while (next.dayOfWeek == DayOfWeek.SATURDAY || next.dayOfWeek == DayOfWeek.SUNDAY) next = next.plusDays(1)
        next
    }
}

/**
 * When the reminder should ring next, strictly after [now].
 * A repeating reminder keeps ringing on schedule even if the task is still open.
 */
fun Task.nextRingAt(now: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): Long? {
    if (done) return null
    val at = reminderAt ?: return null
    if (at > now) return at
    if (repeat == RepeatRule.NONE) return null

    var ring = LocalDateTime.ofInstant(Instant.ofEpochMilli(at), zone)
    val nowTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zone)
    // Guard against a corrupt far-past value looping for too long.
    var steps = 0
    while (!ring.isAfter(nowTime) && steps < 5_000) {
        ring = LocalDateTime.of(repeat.nextDate(ring.toLocalDate()) ?: return null, ring.toLocalTime())
        steps++
    }
    return ring.atZone(zone).toInstant().toEpochMilli()
}

/**
 * Completing a repeating task moves it to its next date instead of finishing it.
 * The reminder keeps the same offset from the due date. Returns null for one-time tasks.
 */
fun Task.nextOccurrence(today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Task? {
    if (repeat == RepeatRule.NONE) return null
    val base = dueDate ?: today
    var next = repeat.nextDate(base) ?: return null
    while (next < today) next = repeat.nextDate(next) ?: return null

    val shift = ChronoUnit.DAYS.between(base, next)
    val reminder = reminderAt?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone).plusDays(shift).atZone(zone).toInstant().toEpochMilli()
    }
    return copy(
        dueDate = next,
        reminderAt = reminder,
        completedAt = null,
        subtasks = subtasks.map { it.copy(done = false) }
    )
}

fun LocalDate.atTimeMillis(time: java.time.LocalTime, zone: ZoneId = ZoneId.systemDefault()): Long =
    atTime(time).atZone(zone).toInstant().toEpochMilli()

fun Long.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zone)
