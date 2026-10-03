package uz.relay.todoapp.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.SettingsRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var toggleTaskUseCase: ToggleTaskUseCase
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var feedback: AlarmFeedback

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L).takeIf { it > 0 } ?: return
        val action = intent.action ?: return

        // Room is suspend; goAsync keeps the receiver alive until the work is done.
        val pending = goAsync()
        scope.launch {
            try {
                handle(action, taskId)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(action: String, taskId: Long) {
        when (action) {
            ACTION_FIRE, ACTION_SNOOZE_FIRE -> ring(taskId, fromSnooze = action == ACTION_SNOOZE_FIRE)

            ACTION_DONE -> {
                stopRinging(taskId)
                toggleTaskUseCase(taskId, done = true).first()
            }

            ACTION_SNOOZE -> {
                stopRinging(taskId)
                val task = taskRepository.getTask(taskId) ?: return
                scheduler.schedule(taskId, System.currentTimeMillis() + SNOOZE_MS, task.alarm, snooze = true)
            }

            ACTION_DISMISS -> feedback.stop()
        }
    }

    private suspend fun ring(taskId: Long, fromSnooze: Boolean) {
        val task = taskRepository.getTask(taskId) ?: return
        if (task.done) return

        notificationHelper.show(task)
        if (settingsRepository.getSettings().vibration) feedback.vibrate(task.alarm)

        if (!fromSnooze) {
            val now = System.currentTimeMillis()
            taskRepository.markRung(taskId, now)
            // A repeating reminder books its next ring right away.
            reminderRepository.sync(task.copy(lastRungAt = now))
        }
    }

    private fun stopRinging(taskId: Long) {
        notificationHelper.cancel(taskId)
        feedback.stop()
    }

    companion object {
        const val ACTION_FIRE = "uz.relay.todoapp.alarm.FIRE"
        const val ACTION_SNOOZE_FIRE = "uz.relay.todoapp.alarm.SNOOZE_FIRE"
        const val ACTION_DONE = "uz.relay.todoapp.alarm.DONE"
        const val ACTION_SNOOZE = "uz.relay.todoapp.alarm.SNOOZE"
        const val ACTION_DISMISS = "uz.relay.todoapp.alarm.DISMISS"
        const val EXTRA_TASK_ID = "task_id"

        private val SNOOZE_MS = TimeUnit.MINUTES.toMillis(10)
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
