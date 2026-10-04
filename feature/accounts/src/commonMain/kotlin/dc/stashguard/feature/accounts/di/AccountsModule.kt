package dc.stashguard.feature.accounts.di

import dc.stashguard.feature.accounts.accounts_list.AccountsViewModel
import dc.stashguard.feature.accounts.add_account.AddAccountViewModel
import dc.stashguard.feature.accounts.details.DetailsAccountViewModel
import dc.stashguard.feature.accounts.edit_account.EditAccountViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val accountsModule = module {
    viewModel { AccountsViewModel(get(), get()) }
    viewModel { AddAccountViewModel(get()) }
    viewModel { (accountId: String) -> EditAccountViewModel(get(), get(), accountId) }

    viewModel { (accountId: String) ->
        DetailsAccountViewModel(
            accountRepository = get(),
            operationRepository = get(),
            deleteAccountUseCase = get(),
            calculateAccountStatistics = get(),
            accountId = accountId
        )
    }
}
