package uz.relay.todoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskDraft

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeTask(id: Long): Flow<Task?>
    fun observeListTasks(listId: Long): Flow<List<Task>>
    fun search(query: String): Flow<List<Task>>

    suspend fun getTask(id: Long): Task?
    suspend fun getTasksWithReminder(): List<Task>
    suspend fun getListTasks(listId: Long): List<Task>

    /** Inserts (id == 0) or updates the task with its subtasks; returns the id. */
    suspend fun save(draft: TaskDraft): Long
    suspend fun update(task: Task)
    suspend fun delete(id: Long)
    suspend fun restore(task: Task)
    suspend fun setSubtaskDone(subtaskId: Long, done: Boolean)
    suspend fun markRung(id: Long, at: Long)
}
