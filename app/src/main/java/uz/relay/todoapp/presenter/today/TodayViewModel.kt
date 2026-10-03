package uz.relay.todoapp.presenter.today

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import uz.relay.todoapp.domain.usecase.GetTodayUseCase
import uz.relay.todoapp.domain.usecase.RestoreTaskUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.presenter.today.TodayContract.Intent
import uz.relay.todoapp.presenter.today.TodayContract.SideEffect
import uz.relay.todoapp.presenter.today.TodayContract.UiTodayState
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val directions: TodayContract.Directions,
    private val getTodayUseCase: GetTodayUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase
) : ViewModel(), TodayContract.ViewModel {

    private var lastDeleted: Task? = null

    override val container: OrbitContainer<UiTodayState, UiTodayState, SideEffect> =
        orbitContainer(UiTodayState()) {
            // Room + the date source, collected only while the tab is visible.
            repeatOnSubscription {
                getTodayUseCase().collect { data ->
                    reduce {
                        state.copy(loading = false, date = data.date, overdue = data.overdue, today = data.today, done = data.done)
                    }
                }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
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
            Intent.NewTask -> intent { directions.newTask(state.date) }
            is Intent.OpenTask -> intent { directions.openTask(intent.id) }
            Intent.OpenSearch -> intent { directions.openSearch() }
            Intent.ToggleDoneSection -> intent { reduce { state.copy(doneExpanded = !state.doneExpanded) } }
        }
    }
}
