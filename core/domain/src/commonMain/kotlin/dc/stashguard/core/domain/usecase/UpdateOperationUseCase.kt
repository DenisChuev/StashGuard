package dc.stashguard.core.domain.usecase

import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.datetime.LocalDate

class LinkedOperationNotFoundException : Exception("Linked transfer operation not found")

/**
 * Saves the edited [original] operation and moves the account balances by the difference.
 * For a transfer, the other side of the transfer is updated as well.
 */
class UpdateOperationUseCase(
    private val accountRepository: AccountRepository,
    private val operationRepository: OperationRepository,
) {
    /**
     * @param toAccountId for a transfer, the other account: the destination when [original] is
     *   the sending side, the source when it is the receiving side. Ignored for other types.
     * @throws LinkedOperationNotFoundException if [original] is a transfer whose other side is missing.
     */
    suspend operator fun invoke(
        original: Operation,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String,
        toAccountId: String,
    ) {
        val updatedOperation = original.copy(
            amount = amount,
            category = categoryId,
            date = date,
            note = note.ifBlank { "" }
        )

        when (original.type) {
            OperationType.REVENUE -> {
                operationRepository.updateOperations(listOf(updatedOperation))
                accountRepository.adjustBalance(original.accountId, amount - original.amount)
            }

            OperationType.EXPENSE -> {
                operationRepository.updateOperations(listOf(updatedOperation))
                accountRepository.adjustBalance(original.accountId, original.amount - amount)
            }

            OperationType.TRANSFER -> updateTransfer(original, updatedOperation, toAccountId)
        }
    }

    private suspend fun updateTransfer(
        original: Operation,
        updatedOperation: Operation,
        otherAccountId: String,
    ) {
        require(otherAccountId != original.accountId) { "Cannot transfer to the same account" }

        val linkedOp =
            original.linkedOperationId?.let { operationRepository.getLinkedOperations(it) }
                ?.firstOrNull { it.id != original.id }
                ?: throw LinkedOperationNotFoundException()

        // Both sides point at each other; the other side lives in the other account
        val updatedOp = updatedOperation.copy(toAccountId = otherAccountId)
        val updatedLinkedOp = linkedOp.copy(
            accountId = otherAccountId,
            amount = updatedOp.amount,
            date = updatedOp.date,
            note = updatedOp.note,
            toAccountId = original.accountId
        )
        operationRepository.updateOperations(listOf(updatedOp, updatedLinkedOp))

        // Undo the old transfer, then apply the new one, each in the right direction
        val oldOtherAccountId = original.toAccountId
        val (oldFrom, oldTo) = original.direction(oldOtherAccountId)
        oldFrom?.let { accountRepository.adjustBalance(it, original.amount) }
        oldTo?.let { accountRepository.adjustBalance(it, -original.amount) }

        val (newFrom, newTo) = original.direction(otherAccountId)
        newFrom?.let { accountRepository.adjustBalance(it, -updatedOp.amount) }
        newTo?.let { accountRepository.adjustBalance(it, updatedOp.amount) }
    }

    /** (sender, receiver) of a transfer between this side's account and [otherAccountId]. */
    private fun Operation.direction(otherAccountId: String?): Pair<String?, String?> =
        if (isIncoming) otherAccountId to accountId else accountId to otherAccountId
}
