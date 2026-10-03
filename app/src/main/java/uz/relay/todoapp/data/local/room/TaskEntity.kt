package uz.relay.todoapp.data.local.room

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = TaskListEntity::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("listId"), Index("dueDate")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    val listId: Long,
    val priority: Int,
    /** LocalDate.toEpochDay() */
    val dueDate: Long?,
    /** Minute of the day */
    val dueTime: Int?,
    val reminderAt: Long?,
    val alarm: Boolean,
    val repeat: String,
    val completedAt: Long?,
    val createdAt: Long,
    val lastRungAt: Long?
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("taskId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val title: String,
    val done: Boolean,
    val position: Int
)

data class TaskWithDetails(
    @Embedded val task: TaskEntity,
    @Relation(parentColumn = "id", entityColumn = "taskId")
    val subtasks: List<SubtaskEntity>,
    @Relation(parentColumn = "listId", entityColumn = "id")
    val list: TaskListEntity?
)
