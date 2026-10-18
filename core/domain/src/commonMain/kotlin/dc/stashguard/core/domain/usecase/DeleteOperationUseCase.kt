package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository
import dc.stashguard.core.domain.repository.TransactionRunner

/**
 * Deletes an operation and undoes its effect on the account balance.
 * Deleting either side of a transfer deletes both sides.
 */
class DeleteOperationUseCase(
    private val accountRepository: AccountRepository,
    private val operationRepository: OperationRepository,
    private val transaction: TransactionRunner,
) {
    suspend operator fun invoke(operation: Operation) = transaction {
        val sides = operation.linkedOperationId
            ?.takeIf { operation.type == OperationType.TRANSFER }
            ?.let { operationRepository.getLinkedOperations(it) }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(operation)

        // adjustBalance skips an account that no longer exists
        sides.forEach { accountRepository.adjustBalance(it.accountId, -it.balanceChange()) }
        operationRepository.deleteOperations(sides.map { it.id })
    }

    /** How much this operation moved the balance of its own account. */
    private fun Operation.balanceChange(): Double = when (type) {
        OperationType.REVENUE -> amount
        OperationType.EXPENSE -> -amount
        OperationType.TRANSFER -> if (isIncoming) amount else -amount
    }
}
