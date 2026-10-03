package uz.relay.todoapp.presenter.upcoming

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Task
import java.time.LocalDate

interface UpcomingContract {
    interface ViewModel : OrbitContainerHost<UiUpcomingState, UiUpcomingState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class SelectDate(val date: LocalDate) : Intent
        data class Toggle(val id: Long, val done: Boolean) : Intent
        data class Delete(val id: Long) : Intent
        data object Undo : Intent
        data class OpenTask(val id: Long) : Intent
        data class QuickAdd(val text: String) : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        data class ShowUndo(val message: String) : SideEffect
    }

    data class UiUpcomingState(
        val loading: Boolean = true,
        val today: LocalDate = LocalDate.now(),
        val selected: LocalDate = LocalDate.now().plusDays(1),
        val groups: List<Pair<LocalDate, List<Task>>> = emptyList()
    ) {
        val days: List<LocalDate> get() = (0L until 14L).map { today.plusDays(it) }
        val busyDays: Set<LocalDate> get() = groups.mapTo(HashSet()) { it.first }
    }

    interface Directions {
        suspend fun openTask(id: Long)
    }
}
