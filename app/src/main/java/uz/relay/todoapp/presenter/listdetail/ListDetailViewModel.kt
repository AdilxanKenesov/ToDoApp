package uz.relay.todoapp.presenter.listdetail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.usecase.DeleteListUseCase
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import uz.relay.todoapp.domain.usecase.GetListTasksUseCase
import uz.relay.todoapp.domain.usecase.GetListUseCase
import uz.relay.todoapp.domain.usecase.ObserveTodayDateUseCase
import uz.relay.todoapp.domain.usecase.QuickAddUseCase
import uz.relay.todoapp.domain.usecase.RestoreTaskUseCase
import uz.relay.todoapp.domain.usecase.SaveListUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.presenter.listdetail.ListDetailContract.Intent
import uz.relay.todoapp.presenter.listdetail.ListDetailContract.SideEffect
import uz.relay.todoapp.presenter.listdetail.ListDetailContract.UiListDetailState
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val directions: ListDetailContract.Directions,
    private val getListUseCase: GetListUseCase,
    private val getListTasksUseCase: GetListTasksUseCase,
    private val observeTodayDateUseCase: ObserveTodayDateUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val quickAddUseCase: QuickAddUseCase,
    private val saveListUseCase: SaveListUseCase,
    private val deleteListUseCase: DeleteListUseCase
) : ViewModel(), ListDetailContract.ViewModel {

    private var lastDeleted: Task? = null

    override val container: OrbitContainer<UiListDetailState, UiListDetailState, SideEffect> =
        orbitContainer(UiListDetailState())

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.Init -> init(intent.listId)
            is Intent.Toggle -> intent {
                toggleTaskUseCase(intent.id, intent.done).collect { result ->
                    result.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            is Intent.Delete -> intent {
                deleteTaskUseCase(intent.id).collect { result ->
                    result.onSuccess { task ->
                        lastDeleted = task
                        postSideEffect(SideEffect.ShowUndo("Task deleted"))
                    }.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            Intent.Undo -> intent {
                val task = lastDeleted ?: return@intent
                lastDeleted = null
                restoreTaskUseCase(task).collect { result ->
                    result.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            is Intent.OpenTask -> intent { directions.openTask(intent.id) }
            is Intent.QuickAdd -> intent {
                val listId = state.listId ?: return@intent
                quickAddUseCase(intent.text, listId = listId).collect { result ->
                    result.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            is Intent.SaveList -> intent {
                saveListUseCase(intent.list).collect { result ->
                    result.onSuccess { postSideEffect(SideEffect.ListSaved) }
                        .onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            Intent.DeleteList -> intent {
                val listId = state.listId ?: return@intent
                deleteListUseCase(listId).collect { result ->
                    result.onSuccess { directions.back() }
                        .onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            Intent.Back -> intent { directions.back() }
        }
    }

    // Init comes from the screen on every composition; only the first one subscribes.
    private fun init(listId: Long) = intent {
        if (state.listId != null) return@intent
        reduce { state.copy(listId = listId) }
        repeatOnSubscription {
            combine(getListUseCase(listId), getListTasksUseCase(listId), observeTodayDateUseCase()) { list, tasks, today ->
                Triple(list, tasks, today)
            }.collect { (list, tasks, today) ->
                reduce { state.copy(loading = false, list = list, tasks = tasks, today = today) }
            }
        }
    }
}
