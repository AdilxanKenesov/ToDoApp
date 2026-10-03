package uz.relay.todoapp.presenter.upcoming

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.model.Task
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import uz.relay.todoapp.domain.usecase.GetUpcomingUseCase
import uz.relay.todoapp.domain.usecase.ObserveTodayDateUseCase
import uz.relay.todoapp.domain.usecase.QuickAddUseCase
import uz.relay.todoapp.domain.usecase.RestoreTaskUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.presenter.upcoming.UpcomingContract.Intent
import uz.relay.todoapp.presenter.upcoming.UpcomingContract.SideEffect
import uz.relay.todoapp.presenter.upcoming.UpcomingContract.UiUpcomingState
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@HiltViewModel
class UpcomingViewModel @Inject constructor(
    private val directions: UpcomingContract.Directions,
    private val getUpcomingUseCase: GetUpcomingUseCase,
    private val observeTodayDateUseCase: ObserveTodayDateUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val restoreTaskUseCase: RestoreTaskUseCase,
    private val quickAddUseCase: QuickAddUseCase
) : ViewModel(), UpcomingContract.ViewModel {

    private var lastDeleted: Task? = null

    override val container: OrbitContainer<UiUpcomingState, UiUpcomingState, SideEffect> =
        orbitContainer(UiUpcomingState()) {
            repeatOnSubscription {
                combine(getUpcomingUseCase(), observeTodayDateUseCase()) { tasks, today -> tasks to today }
                    .collect { (tasks, today) ->
                        reduce {
                            val selected = if (state.selected < today) today.plusDays(1) else state.selected
                            state.copy(
                                loading = false,
                                today = today,
                                selected = selected,
                                groups = tasks.groupBy { it.dueDate!! }.toList()
                            )
                        }
                    }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.SelectDate -> intent { reduce { state.copy(selected = intent.date) } }
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
            is Intent.QuickAdd -> intent {
                quickAddUseCase(intent.text, date = state.selected).collect { result ->
                    result.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            is Intent.OpenTask -> intent { directions.openTask(intent.id) }
        }
    }
}
