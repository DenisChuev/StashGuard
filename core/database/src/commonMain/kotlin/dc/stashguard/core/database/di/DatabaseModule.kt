package dc.stashguard.core.database.di

import dc.stashguard.core.database.AppDatabase
import dc.stashguard.core.database.dao.AccountDao
import dc.stashguard.core.database.dao.CategoryDao
import dc.stashguard.core.database.dao.OperationDao
import org.koin.core.module.Module
import org.koin.dsl.module

val databaseModule = module {
    includes(databasePlatformModule())

    single<AccountDao> { get<AppDatabase>().getAccountDao() }
    single<OperationDao> { get<AppDatabase>().getOperationDao() }
    single<CategoryDao> { get<AppDatabase>().getCategoryDao() }
}

/** Provides the platform's [AppDatabase]. */
internal expect fun databasePlatformModule(): Module
