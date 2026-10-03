package uz.relay.todoapp.presenter.today

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import uz.relay.todoapp.presenter.search.SearchScreen
import javax.inject.Inject

class TodayDirections @Inject constructor(
    private val navigator: AppNavigator
) : TodayContract.Directions {

    override suspend fun openTask(id: Long) {
        navigator.openSheet(EditorScreen(taskId = id))
    }

    override suspend fun openSearch() {
        navigator.navigateTo(SearchScreen())
    }
}
