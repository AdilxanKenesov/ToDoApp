package uz.relay.todoapp.navigation

import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/** A task opened from a notification (or the widget) before the UI was ready. */
@Singleton
class NotificationDeepLink @Inject constructor() {

    sealed interface Target {
        data class OpenTask(val id: Long) : Target
        data object QuickAdd : Target
    }

    private val target = AtomicReference<Target?>(null)

    fun set(value: Target?) {
        target.set(value)
    }

    fun consume(): Target? = target.getAndSet(null)
}
