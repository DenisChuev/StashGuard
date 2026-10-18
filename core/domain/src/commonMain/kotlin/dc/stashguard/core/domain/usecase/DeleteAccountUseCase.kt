package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.model.TRANSFER_CATEGORY_ID
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.repository.TransactionRunner

/**
 * Deletes an account together with all of its operations.
 * The other side of each transfer with this account stays in the other account as a plain
 * revenue or expense, so that account's balance and history don't change.
 */
class DeleteAccountUseCase(
    private val accountRepository: AccountRepository,
    private val operationRepository: OperationRepository,
    private val transaction: TransactionRunner,
) {
    suspend operator fun invoke(accountId: String) = transaction {
        val counterparts = operationRepository.getTransferCounterparts(accountId)
        operationRepository.updateOperations(counterparts.map { it.withoutTransferLink() })
        operationRepository.deleteOperationsByAccount(accountId)
        accountRepository.deleteAccount(accountId)
    }

    private fun Operation.withoutTransferLink() = copy(
        type = if (isIncoming) OperationType.REVENUE else OperationType.EXPENSE,
        // Older transfers stored the category name "Transfer" instead of its id
        category = TRANSFER_CATEGORY_ID,
        linkedOperationId = null,
        toAccountId = null,
        isIncoming = false,
    )
}
