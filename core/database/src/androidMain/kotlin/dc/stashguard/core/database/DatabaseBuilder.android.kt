package dc.stashguard.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dc.stashguard.core.database.AppDatabase

internal fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("StashGuard.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}