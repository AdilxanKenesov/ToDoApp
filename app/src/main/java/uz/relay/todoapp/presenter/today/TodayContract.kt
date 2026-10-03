package uz.relay.todoapp.presenter.today

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Task
import java.time.LocalDate

interface TodayContract {
    interface ViewModel : OrbitContainerHost<UiTodayState, UiTodayState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class Toggle(val id: Long, val done: Boolean) : Intent
        data class Delete(val id: Long) : Intent
        data object Undo : Intent
        data class OpenTask(val id: Long) : Intent
        data object NewTask : Intent
        data object OpenSearch : Intent
        data object ToggleDoneSection : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        data class ShowUndo(val message: String) : SideEffect
    }

    data class UiTodayState(
        val loading: Boolean = true,
        val date: LocalDate = LocalDate.now(),
        val overdue: List<Task> = emptyList(),
        val today: List<Task> = emptyList(),
        val done: List<Task> = emptyList(),
        val doneExpanded: Boolean = false
    ) {
        val total: Int get() = overdue.size + today.size + done.size
        val openCount: Int get() = overdue.size + today.size
    }

    interface Directions {
        suspend fun openTask(id: Long)
        suspend fun openSearch()
        suspend fun newTask(date: LocalDate)
    }
}
