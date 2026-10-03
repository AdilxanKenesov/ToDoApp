package uz.relay.todoapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.navigation.AppNavigationDispatcher
import uz.relay.todoapp.navigation.AppNavigationHandler
import uz.relay.todoapp.navigation.AppNavigator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppNavigationModule {

    @Provides
    @Singleton
    fun provideAppNavigator(): AppNavigator = AppNavigationDispatcher

    @Provides
    @Singleton
    fun provideAppNavigationHandler(): AppNavigationHandler = AppNavigationDispatcher
}
