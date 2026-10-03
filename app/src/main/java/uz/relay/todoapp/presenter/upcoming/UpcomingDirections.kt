package uz.relay.todoapp.presenter.upcoming

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import javax.inject.Inject

class UpcomingDirections @Inject constructor(
    private val navigator: AppNavigator
) : UpcomingContract.Directions {

    override suspend fun openTask(id: Long) {
        navigator.openSheet(EditorScreen(taskId = id))
    }
}
