package uz.relay.todoapp.data.repository_impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.relay.todoapp.data.local.room.CompletionDao
import uz.relay.todoapp.data.local.room.TaskDao
import uz.relay.todoapp.data.local.room.subtaskEntities
import uz.relay.todoapp.data.local.room.toDomain
import uz.relay.todoapp.data.local.room.toEntity
import uz.relay.todoapp.domain.model.Completion
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskDraft
import uz.relay.todoapp.domain.repository.TaskRepository
import uz.relay.todoapp.widget.WidgetUpdater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val dao: TaskDao,
    private val completionDao: CompletionDao,
    private val widgetUpdater: WidgetUpdater
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    override fun observeTask(id: Long): Flow<Task?> =
        dao.observe(id).map { it?.toDomain() }.flowOn(Dispatchers.IO)

    override fun observeListTasks(listId: Long): Flow<List<Task>> =
        dao.observeByList(listId).map { rows -> rows.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    override fun search(query: String): Flow<List<Task>> =
        dao.search(query).map { rows -> rows.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    override suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) { dao.get(id)?.toDomain() }

    override suspend fun getTasksWithReminder(): List<Task> =
        withContext(Dispatchers.IO) { dao.getWithReminder().map { it.toDomain() } }

    override suspend fun getListTasks(listId: Long): List<Task> =
        withContext(Dispatchers.IO) { dao.getByList(listId).map { it.toDomain() } }

    override suspend fun save(draft: TaskDraft): Long = write {
        val existing = if (draft.id == 0L) null else dao.get(draft.id)?.task
        dao.upsertWithSubtasks(draft.toEntity(existing), draft.subtaskEntities())
    }

    override suspend fun update(task: Task) = write {
        dao.upsertWithSubtasks(task.toEntity(), task.subtaskEntities())
        Unit
    }

    override suspend fun delete(id: Long) = write { dao.delete(id) }

    override suspend fun restore(task: Task) = write { dao.restore(task.toEntity(), task.subtaskEntities()) }

    override suspend fun setSubtaskDone(subtaskId: Long, done: Boolean) = write { dao.setSubtaskDone(subtaskId, done) }

    override suspend fun markRung(id: Long, at: Long) = withContext(Dispatchers.IO) { dao.markRung(id, at) }

    override fun observeCompletions(): Flow<List<Completion>> =
        completionDao.observeAll().map { rows -> rows.map { it.toDomain() } }.flowOn(Dispatchers.IO)

    override suspend fun addCompletion(completion: Completion) =
        withContext(Dispatchers.IO) { completionDao.insert(completion.toEntity()) }

    override suspend fun removeLatestCompletion(taskId: Long) =
        withContext(Dispatchers.IO) { completionDao.deleteLatest(taskId) }

    // Every change also refreshes the home-screen widget.
    private suspend fun <T> write(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        block().also { widgetUpdater.refresh() }
    }
}
