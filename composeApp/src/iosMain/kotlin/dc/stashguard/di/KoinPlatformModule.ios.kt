package dc.stashguard.di

import dc.stashguard.data.local.AppDatabase
import dc.stashguard.data.local.getAppDatabase
import dc.stashguard.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<AppDatabase> {
        val builder = getDatabaseBuilder()
        getAppDatabase(builder)
    }
}