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
    /** @throws LinkedOperationNotFoundException if [original] is a transfer whose other side is missing. */
    suspend operator fun invoke(
        original: Operation,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String,
        toAccountId: String,
    ) {
        val oldAmount = original.amount
        val oldToAccountId = original.toAccountId
        val newToAccountId = if (original.type == OperationType.TRANSFER) toAccountId else null

        val updatedOperation = original.copy(
            amount = amount,
            category = categoryId,
            date = date,
            note = note.ifBlank { "" },
            toAccountId = newToAccountId ?: original.toAccountId
        )

        if (original.type == OperationType.TRANSFER) {
            val linkedOp =
                original.linkedOperationId?.let { operationRepository.getLinkedOperations(it) }
                    ?.firstOrNull { it.accountId == original.toAccountId && it.linkedOperationId == original.linkedOperationId }
                    ?: throw LinkedOperationNotFoundException()

            // The other side of the transfer lives in the destination account, so it moves with it
            val updatedLinkedOp = linkedOp.copy(
                accountId = toAccountId,
                amount = amount,
                date = updatedOperation.date,
                note = updatedOperation.note,
                toAccountId = original.accountId
            )
            operationRepository.updateOperations(listOf(updatedOperation, updatedLinkedOp))
        } else {
            operationRepository.updateOperations(listOf(updatedOperation))
        }

        val fromAccountId = original.accountId
        val balanceChange = amount - oldAmount

        when (original.type) {
            OperationType.REVENUE -> accountRepository.adjustBalance(fromAccountId, balanceChange)
            OperationType.EXPENSE -> accountRepository.adjustBalance(fromAccountId, -balanceChange)
            OperationType.TRANSFER -> {
                accountRepository.adjustBalance(fromAccountId, oldAmount)
                if (oldToAccountId != null) {
                    accountRepository.adjustBalance(oldToAccountId, -oldAmount)
                }
                accountRepository.adjustBalance(fromAccountId, -amount)
                if (newToAccountId != null) {
                    accountRepository.adjustBalance(newToAccountId, amount)
                }
            }
        }
    }
}
