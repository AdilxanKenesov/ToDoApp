package uz.relay.todoapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.data.local.room.AppDatabase
import uz.relay.todoapp.data.local.room.ListDao
import uz.relay.todoapp.data.local.room.TaskDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "tick.db")
            .addCallback(AppDatabase.Seed)
            .build()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideListDao(database: AppDatabase): ListDao = database.listDao()
}
