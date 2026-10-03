package uz.relay.todoapp.presenter.listdetail

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.model.TaskList
import java.time.LocalDate

interface ListDetailContract {
    interface ViewModel : OrbitContainerHost<UiListDetailState, UiListDetailState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class Init(val listId: Long) : Intent
        data class Toggle(val id: Long, val done: Boolean) : Intent
        data class Delete(val id: Long) : Intent
        data object Undo : Intent
        data class OpenTask(val id: Long) : Intent
        data class QuickAdd(val text: String) : Intent
        data class SaveList(val list: TaskList) : Intent
        data object DeleteList : Intent
        data object Back : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        data class ShowUndo(val message: String) : SideEffect
        data object ListSaved : SideEffect
    }

    data class UiListDetailState(
        val listId: Long? = null,
        val loading: Boolean = true,
        val list: TaskList? = null,
        val tasks: List<Task> = emptyList(),
        val today: LocalDate = LocalDate.now()
    )

    interface Directions {
        suspend fun back()
        suspend fun openTask(id: Long)
        suspend fun newTask(listId: Long)
    }
}
