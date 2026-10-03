package uz.relay.todoapp.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/** A list row with its task counts, from ListDao's GROUP BY query. */
data class TaskListWithCounts(
    val id: Long,
    val name: String,
    val color: Int,
    val icon: String,
    val taskCount: Int,
    val doneCount: Int
)
