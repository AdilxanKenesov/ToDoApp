package uz.relay.todoapp.presenter.upcoming

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import java.time.LocalDate
import javax.inject.Inject

class UpcomingDirections @Inject constructor(
    private val navigator: AppNavigator
) : UpcomingContract.Directions {

    override suspend fun openTask(id: Long) {
        navigator.navigateTo(EditorScreen(taskId = id))
    }

    override suspend fun newTask(date: LocalDate) {
        navigator.navigateTo(EditorScreen(date = date))
    }
}
