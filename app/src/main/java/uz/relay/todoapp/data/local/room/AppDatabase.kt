package uz.relay.todoapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskListEntity::class, TaskEntity::class, SubtaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun listDao(): ListDao

    /** First launch gets two lists so quick add always has somewhere to go. */
    object Seed : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            db.execSQL("INSERT INTO lists (name, color, icon, position, createdAt) VALUES ('Personal', 1, 'HOME', 0, $now)")
            db.execSQL("INSERT INTO lists (name, color, icon, position, createdAt) VALUES ('Work', 0, 'WORK', 1, $now)")
        }
    }
}
