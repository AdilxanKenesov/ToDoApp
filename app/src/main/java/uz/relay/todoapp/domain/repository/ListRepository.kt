package uz.relay.todoapp.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.relay.todoapp.domain.model.TaskList

interface ListRepository {
    fun observeLists(): Flow<List<TaskList>>
    fun observeList(id: Long): Flow<TaskList?>
    suspend fun firstListId(): Long
    suspend fun findByName(name: String): TaskList?
    suspend fun save(list: TaskList): Long
    suspend fun delete(id: Long)
}
