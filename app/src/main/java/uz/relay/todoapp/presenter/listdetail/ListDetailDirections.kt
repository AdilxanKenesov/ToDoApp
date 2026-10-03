package uz.relay.todoapp.presenter.listdetail

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import javax.inject.Inject

class ListDetailDirections @Inject constructor(
    private val navigator: AppNavigator
) : ListDetailContract.Directions {

    override suspend fun back() {
        navigator.back()
    }

    override suspend fun openTask(id: Long) {
        navigator.openSheet(EditorScreen(taskId = id))
    }

    override suspend fun newTask(listId: Long) {
        navigator.openSheet(EditorScreen(listId = listId))
    }
}
