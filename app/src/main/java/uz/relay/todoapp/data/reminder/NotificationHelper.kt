package uz.relay.todoapp.data.reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.relay.todoapp.MainActivity
import uz.relay.todoapp.R
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.utils.label
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val manager = NotificationManagerCompat.from(context)

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val system = context.getSystemService(NotificationManager::class.java)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val alarms = NotificationChannel(CHANNEL_ALARMS, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Tasks that ring like an alarm clock"
            setSound(
                alarmSound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            // Vibration follows the in-app switch, see AlarmFeedback.
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        val reminders = NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Quiet task reminders"
            enableVibration(false)
        }
        system.createNotificationChannels(listOf(alarms, reminders))
    }

    fun show(task: Task) {
        if (!canNotify()) return

        val text = buildString {
            task.dueTime?.let { append("Due ").append(it.label()) }
            if (task.subtasks.isNotEmpty()) {
                if (isNotEmpty()) append(" · ")
                append("${task.subtasksDone}/${task.subtasks.size} subtasks")
            }
            if (isEmpty()) append(task.listName.ifBlank { "Reminder" })
        }

        val builder = NotificationCompat.Builder(context, if (task.alarm) CHANNEL_ALARMS else CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand))
            .setContentTitle(task.title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (task.alarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openIntent(task.id))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_check, "Done", actionIntent(task.id, AlarmReceiver.ACTION_DONE))
            .addAction(R.drawable.ic_snooze, "Snooze 10 min", actionIntent(task.id, AlarmReceiver.ACTION_SNOOZE))

        if (task.alarm) {
            // Keep ringing until the user reacts, but not forever.
            builder.setTimeoutAfter(TimeUnit.MINUTES.toMillis(5))
            builder.setDeleteIntent(actionIntent(task.id, AlarmReceiver.ACTION_DISMISS))
        }

        val notification = builder.build()
        if (task.alarm) notification.flags = notification.flags or Notification.FLAG_INSISTENT

        try {
            manager.notify(notificationId(task.id), notification)
        } catch (_: SecurityException) {
            // Permission was revoked between the check and the call.
        }
    }

    fun cancel(taskId: Long) {
        manager.cancel(notificationId(taskId))
    }

    fun canNotify(): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && manager.areNotificationsEnabled()
    }

    private fun openIntent(taskId: Long): PendingIntent = PendingIntent.getActivity(
        context,
        notificationId(taskId),
        MainActivity.openTaskIntent(context, taskId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun actionIntent(taskId: Long, action: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(action)
            .putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(
            context,
            (taskId.toString() + action).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationId(taskId: Long): Int = taskId.toInt()

    companion object {
        const val CHANNEL_ALARMS = "alarms"
        const val CHANNEL_REMINDERS = "reminders"
    }
}
