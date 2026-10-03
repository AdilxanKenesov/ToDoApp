package uz.relay.todoapp.presenter.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.SearchTasksUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.presenter.search.SearchContract.Intent
import uz.relay.todoapp.presenter.search.SearchContract.SideEffect
import uz.relay.todoapp.presenter.search.SearchContract.UiSearchState
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val directions: SearchContract.Directions,
    private val searchTasksUseCase: SearchTasksUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase
) : ViewModel(), SearchContract.ViewModel {

    private val query = MutableStateFlow("")

    override val container: OrbitContainer<UiSearchState, UiSearchState, SideEffect> =
        orbitContainer(UiSearchState()) {
            // A new letter cancels the previous Room query.
            repeatOnSubscription {
                query.debounce(120).flatMapLatest { searchTasksUseCase(it) }.collect { results ->
                    reduce { state.copy(results = results) }
                }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.Query -> intent {
                reduce { state.copy(query = intent.text) }
                query.value = intent.text
            }
            is Intent.Toggle -> intent {
                toggleTaskUseCase(intent.id, intent.done).collect { result ->
                    result.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
            is Intent.OpenTask -> intent { directions.openTask(intent.id) }
            Intent.Back -> intent { directions.back() }
        }
    }
}
