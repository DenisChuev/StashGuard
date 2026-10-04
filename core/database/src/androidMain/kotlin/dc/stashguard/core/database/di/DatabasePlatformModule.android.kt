package dc.stashguard.core.database.di

import dc.stashguard.core.database.AppDatabase
import dc.stashguard.core.database.getAppDatabase
import dc.stashguard.core.database.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun databasePlatformModule(): Module = module {
    single<AppDatabase> {
        val builder = getDatabaseBuilder(context = get())
        getAppDatabase(builder)
    }
}