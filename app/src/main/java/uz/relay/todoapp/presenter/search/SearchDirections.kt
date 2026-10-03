package uz.relay.todoapp.presenter.search

import uz.relay.todoapp.navigation.AppNavigator
import uz.relay.todoapp.presenter.editor.EditorScreen
import javax.inject.Inject

class SearchDirections @Inject constructor(
    private val navigator: AppNavigator
) : SearchContract.Directions {

    override suspend fun back() {
        navigator.back()
    }

    override suspend fun openTask(id: Long) {
        navigator.openSheet(EditorScreen(taskId = id))
    }
}
