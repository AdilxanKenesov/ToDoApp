package uz.relay.todoapp.presenter.search

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Task
import java.time.LocalDate

interface SearchContract {
    interface ViewModel : OrbitContainerHost<UiSearchState, UiSearchState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class Query(val text: String) : Intent
        data class Toggle(val id: Long, val done: Boolean) : Intent
        data class OpenTask(val id: Long) : Intent
        data object Back : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
    }

    data class UiSearchState(
        val query: String = "",
        val results: List<Task> = emptyList(),
        val today: LocalDate = LocalDate.now()
    )

    interface Directions {
        suspend fun back()
        suspend fun openTask(id: Long)
    }
}
