package uz.relay.todoapp.presenter.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.model.Subtask
import uz.relay.todoapp.domain.model.TaskDraft
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import uz.relay.todoapp.domain.usecase.GetListsUseCase
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import uz.relay.todoapp.domain.usecase.GetTaskUseCase
import uz.relay.todoapp.domain.usecase.SaveTaskUseCase
import uz.relay.todoapp.presenter.editor.EditorContract.Intent
import uz.relay.todoapp.presenter.editor.EditorContract.SideEffect
import uz.relay.todoapp.presenter.editor.EditorContract.UiEditorState
import uz.relay.todoapp.utils.atTimeMillis
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val directions: EditorContract.Directions,
    private val getTaskUseCase: GetTaskUseCase,
    private val getListsUseCase: GetListsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveTaskUseCase: SaveTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel(), EditorContract.ViewModel {

    override val container: OrbitContainer<UiEditorState, UiEditorState, SideEffect> =
        orbitContainer(UiEditorState())

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.Init -> init(intent)
            is Intent.ChangeTitle -> intent { reduce { state.copy(title = intent.title, titleError = false) } }
            is Intent.ChangeNotes -> intent { reduce { state.copy(notes = intent.notes) } }
            is Intent.SetDate -> intent {
                reduce {
                    if (intent.date == null) state.copy(dueDate = null, dueTime = null) else state.copy(dueDate = intent.date)
                }
            }
            is Intent.SetTime -> setTime(intent)
            is Intent.SetReminder -> intent {
                reduce { state.copy(reminderAt = intent.at, repeat = intent.repeat, alarm = intent.alarm) }
                if (intent.at != null) postSideEffect(SideEffect.CheckAlarmPermissions)
            }
            is Intent.SetPriority -> intent { reduce { state.copy(priority = intent.priority) } }
            is Intent.SetList -> intent { reduce { state.copy(listId = intent.listId) } }
            is Intent.AddSubtask -> intent {
                val title = intent.title.trim()
                if (title.isNotEmpty()) reduce { state.copy(subtasks = state.subtasks + Subtask(title = title)) }
            }
            is Intent.ToggleSubtask -> intent {
                reduce {
                    state.copy(subtasks = state.subtasks.mapIndexed { i, s -> if (i == intent.index) s.copy(done = !s.done) else s })
                }
            }
            is Intent.RemoveSubtask -> intent {
                reduce { state.copy(subtasks = state.subtasks.filterIndexed { i, _ -> i != intent.index }) }
            }
            Intent.Save -> save()
            Intent.Delete -> intent {
                if (state.isNew) return@intent
                deleteTaskUseCase(state.id).collect { result ->
                    result.onSuccess { directions.close() }
                        .onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            Intent.Close -> intent { directions.close() }
        }
    }

    // The sheet sends Init on every composition; only the first one loads, so the
    // typed text survives rotation.
    private fun init(intent: Intent.Init) = intent {
        if (state.initialized) return@intent
        reduce { state.copy(initialized = true, loading = intent.taskId != 0L) }

        val settings = getSettingsUseCase().first()
        val lists = getListsUseCase().first()
        reduce {
            state.copy(
                lists = lists,
                listId = intent.listId ?: lists.firstOrNull()?.id ?: 0,
                dueDate = intent.date,
                alarm = settings.alarmByDefault
            )
        }

        if (intent.taskId != 0L) {
            val task = getTaskUseCase(intent.taskId).first()
            if (task == null) {
                postSideEffect(SideEffect.ShowMessage("Task not found"))
                directions.close()
                return@intent
            }
            reduce {
                state.copy(
                    loading = false,
                    id = task.id,
                    title = task.title,
                    notes = task.notes,
                    listId = task.listId,
                    priority = task.priority,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    reminderAt = task.reminderAt,
                    alarm = task.alarm,
                    repeat = task.repeat,
                    subtasks = task.subtasks
                )
            }
        }
    }

    // Picking a time for the first time also books a reminder at that moment.
    private fun setTime(intent: Intent.SetTime) = intent {
        val time = intent.time
        val date = state.dueDate ?: java.time.LocalDate.now()
        reduce { state.copy(dueTime = time, dueDate = if (time != null) date else state.dueDate) }
        if (time != null && state.reminderAt == null) {
            val at = date.atTimeMillis(time)
            if (at > System.currentTimeMillis()) {
                reduce { state.copy(reminderAt = at) }
                postSideEffect(SideEffect.CheckAlarmPermissions)
            }
        }
    }

    private fun save() = intent {
        if (state.saving || state.loading) return@intent
        if (state.title.isBlank()) {
            reduce { state.copy(titleError = true) }
            return@intent
        }
        reduce { state.copy(saving = true) }

        val draft = TaskDraft(
            id = state.id,
            title = state.title,
            notes = state.notes,
            listId = state.listId,
            priority = state.priority,
            dueDate = state.dueDate,
            dueTime = state.dueTime,
            reminderAt = state.reminderAt,
            alarm = state.alarm,
            repeat = state.repeat,
            subtasks = state.subtasks
        )
        saveTaskUseCase(draft).collect { result ->
            result.onSuccess {
                reduce { state.copy(saving = false) }
                directions.close()
            }.onFailure {
                reduce { state.copy(saving = false) }
                postSideEffect(SideEffect.ShowMessage(it.userMessage()))
            }
        }
    }
}
