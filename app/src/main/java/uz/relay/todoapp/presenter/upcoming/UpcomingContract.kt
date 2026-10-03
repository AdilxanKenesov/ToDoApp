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
        data class NewTask(val date: LocalDate) : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        data class ShowUndo(val message: String) : SideEffect
    }

    data class UiUpcomingState(
        val loading: Boolean = true,
        val today: LocalDate = LocalDate.now(),
        /** A day picked in the strip; null shows every day. */
        val selected: LocalDate? = null,
        val groups: List<Pair<LocalDate, List<Task>>> = emptyList()
    ) {
        /** Today has its own tab, so the strip starts tomorrow. */
        val days: List<LocalDate> get() = (1L..14L).map { today.plusDays(it) }
        val busyDays: Set<LocalDate> get() = groups.mapTo(HashSet()) { it.first }
        val visibleGroups: List<Pair<LocalDate, List<Task>>>
            get() = selected?.let { day -> listOf(day to groups.firstOrNull { it.first == day }?.second.orEmpty()) } ?: groups
    }

    interface Directions {
        suspend fun openTask(id: Long)
        suspend fun newTask(date: LocalDate)
    }
}
