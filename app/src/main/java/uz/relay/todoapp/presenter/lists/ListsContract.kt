package uz.relay.todoapp.presenter.lists

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.TaskList

interface ListsContract {
    interface ViewModel : OrbitContainerHost<UiListsState, UiListsState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data class OpenList(val id: Long) : Intent
        data class CreateList(val list: TaskList) : Intent
        data object OpenSearch : Intent
    }

    sealed interface SideEffect {
        data class ShowMessage(val message: String) : SideEffect
        data object ListCreated : SideEffect
    }

    data class UiListsState(
        val loading: Boolean = true,
        val lists: List<TaskList> = emptyList()
    )

    interface Directions {
        suspend fun openList(id: Long)
        suspend fun openSearch()
    }
}
