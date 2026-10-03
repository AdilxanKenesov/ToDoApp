package uz.relay.todoapp.presenter.lists

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.GetListsUseCase
import uz.relay.todoapp.domain.usecase.SaveListUseCase
import uz.relay.todoapp.presenter.lists.ListsContract.Intent
import uz.relay.todoapp.presenter.lists.ListsContract.SideEffect
import uz.relay.todoapp.presenter.lists.ListsContract.UiListsState
import uz.relay.todoapp.utils.userMessage
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(
    private val directions: ListsContract.Directions,
    private val getListsUseCase: GetListsUseCase,
    private val saveListUseCase: SaveListUseCase
) : ViewModel(), ListsContract.ViewModel {

    override val container: OrbitContainer<UiListsState, UiListsState, SideEffect> =
        orbitContainer(UiListsState()) {
            repeatOnSubscription {
                getListsUseCase().collect { lists -> reduce { state.copy(loading = false, lists = lists) } }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            is Intent.OpenList -> intent { directions.openList(intent.id) }
            Intent.OpenSearch -> intent { directions.openSearch() }
            is Intent.CreateList -> intent {
                saveListUseCase(intent.list).collect { result ->
                    result.onSuccess { id ->
                        postSideEffect(SideEffect.ListCreated)
                        directions.openList(id)
                    }.onFailure { postSideEffect(SideEffect.ShowMessage(it.userMessage())) }
                }
            }
        }
    }
}
