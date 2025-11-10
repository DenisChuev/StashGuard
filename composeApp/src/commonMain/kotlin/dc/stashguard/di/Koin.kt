package dc.stashguard.di

import dc.stashguard.data.local.AccountDao
import dc.stashguard.data.local.AppDatabase
import dc.stashguard.data.local.CategoryDao
import dc.stashguard.data.local.OperationDao
import dc.stashguard.model.OperationType
import dc.stashguard.screens.accounts.accounts_list.AccountsViewModel
import dc.stashguard.screens.accounts.add_account.AddAccountViewModel
import dc.stashguard.screens.accounts.details.DetailsAccountViewModel
import dc.stashguard.screens.accounts.edit_account.EditAccountViewModel
import dc.stashguard.screens.categories.CategoriesViewModel
import dc.stashguard.screens.operations.OperationsViewModel
import dc.stashguard.screens.operations.add_operation.AddOperationViewModel
import dc.stashguard.screens.operations.edit_operation.EditOperationViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val databaseModule = module {
    single<AccountDao> { get<AppDatabase>().getAccountDao() }
    single<OperationDao> { get<AppDatabase>().getOperationDao() }
    single<CategoryDao> { get<AppDatabase>().getCategoryDao() }
}

val viewModelModule = module {
    viewModel { AccountsViewModel(get()) }
    viewModel { AddAccountViewModel(get()) }
    viewModel { (accountId: String) -> EditAccountViewModel(get(), accountId) }

    viewModel { (accountId: String) ->
        DetailsAccountViewModel(
            accountDao = get(),
            operationDao = get(),
            accountId = accountId
        )
    }

    viewModel { (accountId: String, operationType: OperationType) ->
        AddOperationViewModel(
            accountDao = get(),
            operationDao = get(),
            categoryDao = get(),
            accountId = accountId,
            operationType = operationType
        )
    }

    viewModel { CategoriesViewModel(get()) }
    viewModel { OperationsViewModel(get(), get()) }
    viewModel { (operationId: String) -> EditOperationViewModel(get(), get(), get(), operationId) }
}

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            databaseModule + viewModelModule + platformModule()
        )
    }
}