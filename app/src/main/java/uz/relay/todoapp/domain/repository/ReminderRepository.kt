package uz.relay.todoapp.domain.repository

import uz.relay.todoapp.domain.model.Task

interface ReminderRepository {
    /** Schedules the task's reminder, or cancels it when there is nothing left to ring. */
    fun sync(task: Task)
    fun cancel(taskId: Long)
    suspend fun rescheduleAll()
}
