package uz.relay.todoapp.data.repository_impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.relay.todoapp.data.reminder.NotificationHelper
import uz.relay.todoapp.data.reminder.ReminderScheduler
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.utils.nextRingAt
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val scheduler: ReminderScheduler,
    private val notificationHelper: NotificationHelper,
    private val taskRepository: TaskRepository
) : ReminderRepository {

    // The reminder lives on the task itself, so there is nothing to keep in sync but the alarm.
    override fun sync(task: Task) {
        val at = task.nextRingAt()
        if (at == null) {
            scheduler.cancel(task.id)
        } else {
            scheduler.schedule(task.id, at, task.alarm)
        }
        if (task.done) notificationHelper.cancel(task.id)
    }

    override fun cancel(taskId: Long) {
        scheduler.cancel(taskId)
        notificationHelper.cancel(taskId)
    }

    override suspend fun rescheduleAll() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        taskRepository.getTasksWithReminder().forEach { task ->
            val at = task.nextRingAt(now)
            when {
                at != null -> scheduler.schedule(task.id, at, task.alarm)
                task.missedWhileOff(now) -> {
                    // It was due while the phone was off: ring once now instead of dropping it.
                    notificationHelper.show(task)
                    taskRepository.markRung(task.id, now)
                }
            }
        }
    }

    private fun Task.missedWhileOff(now: Long): Boolean {
        val at = reminderAt ?: return false
        return repeat == RepeatRule.NONE &&
                at <= now &&
                now - at < TimeUnit.HOURS.toMillis(24) &&
                (lastRungAt == null || lastRungAt < at)
    }
}
