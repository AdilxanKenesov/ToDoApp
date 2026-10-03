package uz.relay.todoapp.presenter.editor

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Priority
import uz.relay.todoapp.domain.model.RepeatRule
import uz.relay.todoapp.domain.model.Subtask
import uz.relay.todoapp.domain.model.TaskList
import java.time.LocalDate
import java.time.LocalTime

interface EditorContract {
    interface ViewModel : OrbitContainerHost<UiEditorState, UiEditorState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class Init(val taskId: Long, val listId: Long?, val date: LocalDate?) : Intent
        data class ChangeTitle(val title: String) : Intent
        data class ChangeNotes(val notes: String) : Intent
        data class SetDate(val date: LocalDate?) : Intent
        data class SetTime(val time: LocalTime?) : Intent
        data class SetReminder(val at: Long?, val repeat: RepeatRule, val alarm: Boolean) : Intent
        data class SetPriority(val priority: Priority) : Intent
        data class SetList(val listId: Long) : Intent
        data class AddSubtask(val title: String) : Intent
        data class ToggleSubtask(val index: Int) : Intent
        data class RemoveSubtask(val index: Int) : Intent
        data object Save : Intent
        data object Delete : Intent
        data object Close : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        /** A reminder was set: the screen checks notification and exact-alarm permissions. */
        data object CheckAlarmPermissions : SideEffect
    }

    data class UiEditorState(
        val initialized: Boolean = false,
        val loading: Boolean = false,
        val saving: Boolean = false,
        val id: Long = 0,
        val title: String = "",
        val notes: String = "",
        val listId: Long = 0,
        val lists: List<TaskList> = emptyList(),
        val priority: Priority = Priority.NONE,
        val dueDate: LocalDate? = null,
        val dueTime: LocalTime? = null,
        val reminderAt: Long? = null,
        val alarm: Boolean = true,
        val repeat: RepeatRule = RepeatRule.NONE,
        val subtasks: List<Subtask> = emptyList(),
        val titleError: Boolean = false
    ) {
        val isNew: Boolean get() = id == 0L
        val list: TaskList? get() = lists.firstOrNull { it.id == listId }
    }

    interface Directions {
        suspend fun close()
    }
}
