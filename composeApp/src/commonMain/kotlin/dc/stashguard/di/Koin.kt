package dc.stashguard.di

import dc.stashguard.data.local.AccountDao
import dc.stashguard.data.local.AppDatabase
import dc.stashguard.screens.accounts.accounts_list.AccountsViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun commonModule(): Module = module {
    single<AccountDao> { get<AppDatabase>().getAccountDao() }
    singleOf(::AccountsViewModel)
}

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            commonModule() + platformModule()
        )
    }
}