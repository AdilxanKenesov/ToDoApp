package uz.relay.todoapp.presenter.today

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import uz.relay.todoapp.presenter.search.SearchScreen
import java.time.LocalDate
import javax.inject.Inject

class TodayDirections @Inject constructor(
    private val navigator: AppNavigator
) : TodayContract.Directions {

    override suspend fun openTask(id: Long) {
        navigator.navigateTo(EditorScreen(taskId = id))
    }

    override suspend fun newTask(date: LocalDate) {
        navigator.navigateTo(EditorScreen(date = date))
    }

    override suspend fun openSearch() {
        navigator.navigateTo(SearchScreen())
    }
}
