package uz.relay.todoapp.presenter.editor

import uz.relay.todoapp.navigation.AppNavigator
import javax.inject.Inject

class EditorDirections @Inject constructor(
    private val navigator: AppNavigator
) : EditorContract.Directions {

    override suspend fun close() {
        navigator.back()
    }
}
