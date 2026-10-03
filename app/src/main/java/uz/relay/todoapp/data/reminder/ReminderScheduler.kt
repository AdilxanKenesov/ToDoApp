package uz.relay.todoapp.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.relay.todoapp.MainActivity
import uz.relay.todoapp.data.system.SystemSources
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val systemSources: SystemSources
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Alarm-style tasks use setAlarmClock: exact, survives Doze and shows the alarm icon
     * in the status bar. Plain reminders use an exact idle alarm. Without the exact-alarm
     * permission both fall back to an inexact alarm that Android may delay a little.
     */
    fun schedule(taskId: Long, at: Long, alarm: Boolean, snooze: Boolean = false) {
        val operation = alarmIntent(taskId, if (snooze) AlarmReceiver.ACTION_SNOOZE_FIRE else AlarmReceiver.ACTION_FIRE, snooze)
        when {
            !systemSources.canScheduleExact() ->
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
            alarm ->
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(at, showIntent(taskId)), operation)
            else ->
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, operation)
        }
    }

    fun cancel(taskId: Long) {
        alarmManager.cancel(alarmIntent(taskId, AlarmReceiver.ACTION_FIRE, snooze = false))
        alarmManager.cancel(alarmIntent(taskId, AlarmReceiver.ACTION_SNOOZE_FIRE, snooze = true))
    }

    private fun alarmIntent(taskId: Long, action: String, snooze: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(action)
            .putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(
            context,
            requestCode(taskId, snooze),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showIntent(taskId: Long): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode(taskId, snooze = false),
        MainActivity.openTaskIntent(context, taskId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Even codes for the main alarm, odd ones for its snooze.
    private fun requestCode(taskId: Long, snooze: Boolean): Int = (taskId.toInt() shl 1) or (if (snooze) 1 else 0)
}
