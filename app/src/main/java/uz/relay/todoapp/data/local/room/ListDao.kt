package uz.relay.todoapp.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {

    @Query(
        """
        SELECT l.id, l.name, l.color, l.icon,
               COUNT(t.id) AS taskCount,
               COALESCE(SUM(CASE WHEN t.completedAt IS NOT NULL THEN 1 ELSE 0 END), 0) AS doneCount
        FROM lists l LEFT JOIN tasks t ON t.listId = l.id
        GROUP BY l.id
        ORDER BY l.position, l.id
        """
    )
    fun observeWithCounts(): Flow<List<TaskListWithCounts>>

    @Query(
        """
        SELECT l.id, l.name, l.color, l.icon,
               COUNT(t.id) AS taskCount,
               COALESCE(SUM(CASE WHEN t.completedAt IS NOT NULL THEN 1 ELSE 0 END), 0) AS doneCount
        FROM lists l LEFT JOIN tasks t ON t.listId = l.id
        WHERE l.id = :id
        GROUP BY l.id
        """
    )
    fun observeWithCounts(id: Long): Flow<TaskListWithCounts?>

    @Query("SELECT id FROM lists ORDER BY position, id LIMIT 1")
    suspend fun firstId(): Long?

    @Query("SELECT * FROM lists WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): TaskListEntity?

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun get(id: Long): TaskListEntity?

    @Query("SELECT COALESCE(MAX(position), 0) FROM lists")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(list: TaskListEntity): Long

    @Update
    suspend fun update(list: TaskListEntity)

    @Query("DELETE FROM lists WHERE id = :id")
    suspend fun delete(id: Long)
}
