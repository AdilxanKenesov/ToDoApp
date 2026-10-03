package uz.relay.todoapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.relay.todoapp.data.local.room.AppDatabase
import uz.relay.todoapp.data.local.room.CompletionDao
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideListDao(database: AppDatabase): ListDao = database.listDao()

    @Provides
    fun provideCompletionDao(database: AppDatabase): CompletionDao = database.completionDao()

    private companion object {
        /** Adds the completion log and fills it from tasks that are already done. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `completions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `taskId` INTEGER, " +
                        "`listId` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, `date` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , " +
                        "FOREIGN KEY(`listId`) REFERENCES `lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_completions_taskId` ON `completions` (`taskId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_completions_listId` ON `completions` (`listId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_completions_date` ON `completions` (`date`)")
                // date = local epoch day of completedAt (julian day 2440587.5 is 1970-01-01).
                db.execSQL(
                    "INSERT INTO completions (taskId, listId, completedAt, date) " +
                        "SELECT id, listId, completedAt, " +
                        "CAST(julianday(date(completedAt / 1000, 'unixepoch', 'localtime')) - 2440587.5 AS INTEGER) " +
                        "FROM tasks WHERE completedAt IS NOT NULL"
                )
            }
        }
    }
}
