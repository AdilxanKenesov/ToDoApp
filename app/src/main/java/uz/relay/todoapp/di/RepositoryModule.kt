package uz.relay.todoapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.data.repository_impl.ListRepositoryImpl
import uz.relay.todoapp.data.repository_impl.ReminderRepositoryImpl
import uz.relay.todoapp.data.repository_impl.SettingsRepositoryImpl
import uz.relay.todoapp.data.repository_impl.SystemRepositoryImpl
import uz.relay.todoapp.data.repository_impl.TaskRepositoryImpl
import uz.relay.todoapp.domain.repository.ListRepository
import uz.relay.todoapp.domain.repository.ReminderRepository
import uz.relay.todoapp.domain.repository.SettingsRepository
import uz.relay.todoapp.domain.repository.SystemRepository
import uz.relay.todoapp.domain.repository.TaskRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Singleton
    @Binds
    fun bindListRepository(impl: ListRepositoryImpl): ListRepository

    @Singleton
    @Binds
    fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Singleton
    @Binds
    fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Singleton
    @Binds
    fun bindSystemRepository(impl: SystemRepositoryImpl): SystemRepository
}
