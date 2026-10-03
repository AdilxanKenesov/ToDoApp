package uz.relay.todoapp.presenter.stats

import org.orbitmvi.orbit.OrbitContainerHost
import uz.relay.todoapp.domain.model.Stats

interface StatsContract {
    interface ViewModel : OrbitContainerHost<UiStatsState, UiStatsState, SideEffect> {
        fun onEventDispatcher(intent: Intent)
    }

    sealed interface Intent {
        data object OpenSettings : Intent
    }

    sealed interface SideEffect

    data class UiStatsState(
        val loading: Boolean = true,
        val stats: Stats = Stats()
    )

    interface Directions {
        suspend fun openSettings()
    }
}
