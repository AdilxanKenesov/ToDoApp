package uz.relay.todoapp.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks")
    fun observeAll(): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: Long): Flow<TaskWithDetails?>

    @Transaction
    @Query("SELECT * FROM tasks WHERE listId = :listId")
    fun observeByList(listId: Long): Flow<List<TaskWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'
        ORDER BY completedAt IS NOT NULL, createdAt DESC
        """
    )
    fun search(query: String): Flow<List<TaskWithDetails>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): TaskWithDetails?

    @Transaction
    @Query("SELECT * FROM tasks WHERE reminderAt IS NOT NULL AND completedAt IS NULL")
    suspend fun getWithReminder(): List<TaskWithDetails>

    @Transaction
    @Query("SELECT * FROM tasks WHERE listId = :listId")
    suspend fun getByList(listId: Long): List<TaskWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasks(taskId: Long)

    @Query("UPDATE subtasks SET done = :done WHERE id = :id")
    suspend fun setSubtaskDone(id: Long, done: Boolean)

    @Query("UPDATE tasks SET lastRungAt = :at WHERE id = :id")
    suspend fun markRung(id: Long, at: Long)

    /** Writes the task and replaces its subtasks in one transaction. */
    @Transaction
    suspend fun upsertWithSubtasks(task: TaskEntity, subtasks: List<SubtaskEntity>): Long {
        val id = if (task.id == 0L) insert(task) else task.id.also { update(task) }
        deleteSubtasks(id)
        insertSubtasks(subtasks.mapIndexed { index, subtask -> subtask.copy(taskId = id, position = index) })
        return id
    }

    @Transaction
    suspend fun restore(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        insert(task)
        insertSubtasks(subtasks)
    }
}
