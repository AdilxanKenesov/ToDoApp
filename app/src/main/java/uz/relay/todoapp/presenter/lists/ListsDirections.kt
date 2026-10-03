package uz.relay.todoapp.presenter.lists

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.listdetail.ListDetailScreen
import uz.relay.todoapp.presenter.search.SearchScreen
import javax.inject.Inject

class ListsDirections @Inject constructor(
    private val navigator: AppNavigator
) : ListsContract.Directions {

    override suspend fun openList(id: Long) {
        navigator.navigateTo(ListDetailScreen(listId = id))
    }

    override suspend fun openSearch() {
        navigator.navigateTo(SearchScreen())
    }
}
