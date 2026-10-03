package uz.relay.todoapp.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.domain.usecase.GetTodayUseCase
import uz.relay.todoapp.domain.usecase.ToggleTaskUseCase

/** Glance widgets are not Android entry points, so they reach Hilt through this. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTodayUseCase(): GetTodayUseCase
    fun toggleTaskUseCase(): ToggleTaskUseCase
}
