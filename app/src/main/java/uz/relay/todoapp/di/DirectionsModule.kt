package uz.relay.todoapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.presenter.editor.EditorContract
import uz.relay.todoapp.presenter.editor.EditorDirections
import uz.relay.todoapp.presenter.listdetail.ListDetailContract
import uz.relay.todoapp.presenter.listdetail.ListDetailDirections
import uz.relay.todoapp.presenter.lists.ListsContract
import uz.relay.todoapp.presenter.lists.ListsDirections
import uz.relay.todoapp.presenter.onboarding.OnboardingContract
import uz.relay.todoapp.presenter.onboarding.OnboardingDirections
import uz.relay.todoapp.presenter.search.SearchContract
import uz.relay.todoapp.presenter.search.SearchDirections
import uz.relay.todoapp.presenter.settings.SettingsContract
import uz.relay.todoapp.presenter.settings.SettingsDirections
import uz.relay.todoapp.presenter.splash.SplashContract
import uz.relay.todoapp.presenter.stats.StatsContract
import uz.relay.todoapp.presenter.stats.StatsDirections
import uz.relay.todoapp.presenter.splash.SplashDirections
import uz.relay.todoapp.presenter.today.TodayContract
import uz.relay.todoapp.presenter.today.TodayDirections
import uz.relay.todoapp.presenter.upcoming.UpcomingContract
import uz.relay.todoapp.presenter.upcoming.UpcomingDirections
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DirectionsModule {

    @Singleton
    @Binds
    fun bindSplashDirections(impl: SplashDirections): SplashContract.Directions

    @Singleton
    @Binds
    fun bindOnboardingDirections(impl: OnboardingDirections): OnboardingContract.Directions

    @Singleton
    @Binds
    fun bindTodayDirections(impl: TodayDirections): TodayContract.Directions

    @Singleton
    @Binds
    fun bindUpcomingDirections(impl: UpcomingDirections): UpcomingContract.Directions

    @Singleton
    @Binds
    fun bindListsDirections(impl: ListsDirections): ListsContract.Directions

    @Singleton
    @Binds
    fun bindListDetailDirections(impl: ListDetailDirections): ListDetailContract.Directions

    @Singleton
    @Binds
    fun bindSearchDirections(impl: SearchDirections): SearchContract.Directions

    @Singleton
    @Binds
    fun bindEditorDirections(impl: EditorDirections): EditorContract.Directions

    @Singleton
    @Binds
    fun bindSettingsDirections(impl: SettingsDirections): SettingsContract.Directions

    @Singleton
    @Binds
    fun bindStatsDirections(impl: StatsDirections): StatsContract.Directions
}
