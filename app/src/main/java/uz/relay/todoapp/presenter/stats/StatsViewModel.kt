package uz.relay.todoapp.presenter.stats

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.viewmodel.orbitContainer
import uz.relay.todoapp.domain.usecase.GetStatsUseCase
import uz.relay.todoapp.presenter.stats.StatsContract.Intent
import uz.relay.todoapp.presenter.stats.StatsContract.SideEffect
import uz.relay.todoapp.presenter.stats.StatsContract.UiStatsState
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val directions: StatsContract.Directions,
    private val getStatsUseCase: GetStatsUseCase
) : ViewModel(), StatsContract.ViewModel {

    override val container: OrbitContainer<UiStatsState, UiStatsState, SideEffect> =
        orbitContainer(UiStatsState()) {
            repeatOnSubscription {
                getStatsUseCase().collect { stats -> reduce { state.copy(loading = false, stats = stats) } }
            }
        }

    override fun onEventDispatcher(intent: Intent) {
        when (intent) {
            Intent.OpenSettings -> intent { directions.openSettings() }
        }
    }
}
