package uz.relay.todoapp.data.local.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * One row per check-off. A repeating task moves on to its next date when it is done,
 * so the task row alone cannot tell how often it was completed.
 */
@Entity(
    tableName = "completions",
    foreignKeys = [
        // History survives a deleted task, but not a deleted list.
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = TaskListEntity::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("taskId"), Index("listId"), Index("date")]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long?,
    val listId: Long,
    val completedAt: Long,
    /** LocalDate.toEpochDay() */
    val date: Long
)

@Dao
interface CompletionDao {

    @Query("SELECT * FROM completions")
    fun observeAll(): Flow<List<CompletionEntity>>

    @Insert
    suspend fun insert(completion: CompletionEntity)

    /** Un-checking a task takes back its latest check-off. */
    @Query("DELETE FROM completions WHERE id = (SELECT id FROM completions WHERE taskId = :taskId ORDER BY completedAt DESC LIMIT 1)")
    suspend fun deleteLatest(taskId: Long)
}
