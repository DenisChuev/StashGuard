package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.Account
import dc.stashguard.core.domain.repository.AccountRepository

class AddAccountUseCase(private val accountRepository: AccountRepository) {
    suspend operator fun invoke(account: Account) {
        // New accounts go to the end of the user-defined order
        val position = accountRepository.getMaxPosition() + 1
        accountRepository.addAccount(account.copy(position = position))
    }
}
