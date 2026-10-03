package uz.relay.todoapp.data.repository_impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.relay.todoapp.data.local.room.ListDao
import uz.relay.todoapp.data.local.room.TaskListEntity
import uz.relay.todoapp.data.local.room.toDomain
import uz.relay.todoapp.domain.model.TaskList
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.widget.WidgetUpdater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRepositoryImpl @Inject constructor(
    private val dao: ListDao,
    private val widgetUpdater: WidgetUpdater
) : ListRepository {

    override fun observeLists(): Flow<List<TaskList>> =
        dao.observeWithCounts().map { rows -> rows.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    override fun observeList(id: Long): Flow<TaskList?> =
        dao.observeWithCounts(id).map { it?.toDomain() }.flowOn(Dispatchers.IO)

    override suspend fun firstListId(): Long = withContext(Dispatchers.IO) {
        dao.firstId() ?: dao.insert(TaskListEntity(name = "Personal", color = 1, icon = "HOME"))
    }

    override suspend fun findByName(name: String): TaskList? = withContext(Dispatchers.IO) {
        dao.findByName(name)?.let { TaskList(it.id, it.name, it.color, uz.relay.todoapp.domain.model.ListIcon.from(it.icon)) }
    }

    override suspend fun save(list: TaskList): Long = withContext(Dispatchers.IO) {
        val existing = if (list.id == 0L) null else dao.get(list.id)
        if (existing == null) {
            dao.insert(TaskListEntity(name = list.name, color = list.color, icon = list.icon.name, position = dao.maxPosition() + 1))
        } else {
            dao.update(existing.copy(name = list.name, color = list.color, icon = list.icon.name))
            existing.id
        }
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
        widgetUpdater.refresh()
    }
}
