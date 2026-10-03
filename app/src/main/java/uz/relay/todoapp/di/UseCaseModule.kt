package uz.relay.todoapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.domain.usecase.DeleteListUseCase
import uz.relay.todoapp.domain.usecase.DeleteTaskUseCase
import uz.relay.todoapp.domain.usecase.GetListTasksUseCase
import uz.relay.todoapp.domain.usecase.GetListUseCase
import uz.relay.todoapp.domain.usecase.GetListsUseCase
import uz.relay.todoapp.domain.usecase.GetSettingsUseCase
import uz.relay.todoapp.domain.usecase.GetTaskUseCase
import uz.relay.todoapp.domain.usecase.GetTodayUseCase
import uz.relay.todoapp.domain.usecase.GetUpcomingUseCase
import uz.relay.todoapp.domain.usecase.ObserveExactAlarmsUseCase
import uz.relay.todoapp.domain.usecase.ObserveTodayDateUseCase
import uz.relay.todoapp.domain.usecase.QuickAddUseCase
import uz.relay.todoapp.domain.usecase.RestoreTaskUseCase
import uz.relay.todoapp.domain.usecase.SaveListUseCase
import uz.relay.todoapp.domain.usecase.SaveTaskUseCase
import uz.relay.todoapp.domain.usecase.SearchTasksUseCase
import uz.relay.todoapp.domain.usecase.ToggleSubtaskUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase
import uz.relay.todoapp.domain.usecase.UpdateSettingsUseCase
import uz.relay.todoapp.domain.usecase.impl.DeleteListUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.DeleteTaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetListTasksUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetListUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetListsUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetSettingsUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetTaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetTodayUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.GetUpcomingUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.ObserveExactAlarmsUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.ObserveTodayDateUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.QuickAddUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.RestoreTaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.SaveListUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.SaveTaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.SearchTasksUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.ToggleSubtaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.ToggleTaskUseCaseImpl
import uz.relay.todoapp.domain.usecase.impl.UpdateSettingsUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
interface UseCaseModule {

    @Binds
    fun bindDeleteListUseCase(impl: DeleteListUseCaseImpl): DeleteListUseCase

    @Binds
    fun bindDeleteTaskUseCase(impl: DeleteTaskUseCaseImpl): DeleteTaskUseCase

    @Binds
    fun bindGetListTasksUseCase(impl: GetListTasksUseCaseImpl): GetListTasksUseCase

    @Binds
    fun bindGetListUseCase(impl: GetListUseCaseImpl): GetListUseCase

    @Binds
    fun bindGetListsUseCase(impl: GetListsUseCaseImpl): GetListsUseCase

    @Binds
    fun bindGetSettingsUseCase(impl: GetSettingsUseCaseImpl): GetSettingsUseCase

    @Binds
    fun bindGetTaskUseCase(impl: GetTaskUseCaseImpl): GetTaskUseCase

    @Binds
    fun bindGetTodayUseCase(impl: GetTodayUseCaseImpl): GetTodayUseCase

    @Binds
    fun bindGetUpcomingUseCase(impl: GetUpcomingUseCaseImpl): GetUpcomingUseCase

    @Binds
    fun bindObserveExactAlarmsUseCase(impl: ObserveExactAlarmsUseCaseImpl): ObserveExactAlarmsUseCase

    @Binds
    fun bindObserveTodayDateUseCase(impl: ObserveTodayDateUseCaseImpl): ObserveTodayDateUseCase

    @Binds
    fun bindQuickAddUseCase(impl: QuickAddUseCaseImpl): QuickAddUseCase

    @Binds
    fun bindRestoreTaskUseCase(impl: RestoreTaskUseCaseImpl): RestoreTaskUseCase

    @Binds
    fun bindSaveListUseCase(impl: SaveListUseCaseImpl): SaveListUseCase

    @Binds
    fun bindSaveTaskUseCase(impl: SaveTaskUseCaseImpl): SaveTaskUseCase

    @Binds
    fun bindSearchTasksUseCase(impl: SearchTasksUseCaseImpl): SearchTasksUseCase

    @Binds
    fun bindToggleSubtaskUseCase(impl: ToggleSubtaskUseCaseImpl): ToggleSubtaskUseCase

    @Binds
    fun bindToggleTaskUseCase(impl: ToggleTaskUseCaseImpl): ToggleTaskUseCase

    @Binds
    fun bindUpdateSettingsUseCase(impl: UpdateSettingsUseCaseImpl): UpdateSettingsUseCase
}
