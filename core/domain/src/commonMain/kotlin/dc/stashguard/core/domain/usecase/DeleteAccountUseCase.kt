package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository

/** Deletes an account together with all of its operations. */
class DeleteAccountUseCase(
    private val accountRepository: AccountRepository,
    private val operationRepository: OperationRepository,
) {
    suspend operator fun invoke(accountId: String) {
        operationRepository.deleteOperationsByAccount(accountId)
        accountRepository.deleteAccount(accountId)
    }
}
