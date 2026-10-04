@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.core.domain.usecase

import dc.stashguard.core.common.DateUtils
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Records an operation and applies it to the account balances.
 * A transfer is stored as two operations, one per account, sharing a `linkedOperationId`.
 */
class AddOperationUseCase(
    private val accountRepository: AccountRepository,
    private val operationRepository: OperationRepository,
) {
    suspend operator fun invoke(
        accountId: String,
        type: OperationType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String,
        toAccountId: String,
    ) {
        when (type) {
            OperationType.REVENUE -> addSingleOperation(accountId, type, amount, categoryId, date, note)
            OperationType.EXPENSE -> addSingleOperation(accountId, type, amount, categoryId, date, note)
            OperationType.TRANSFER -> addTransfer(accountId, toAccountId, amount, date, note)
        }
    }

    private suspend fun addSingleOperation(
        accountId: String,
        type: OperationType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String,
    ) {
        val operation = Operation(
            id = Uuid.random().toString(),
            accountId = accountId,
            type = type,
            amount = amount,
            category = categoryId,
            date = date,
            note = note,
            createdAt = DateUtils.currentInstant(),
        )
        operationRepository.addOperations(listOf(operation))
        accountRepository.adjustBalance(accountId, if (type == OperationType.REVENUE) amount else -amount)
    }

    private suspend fun addTransfer(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        date: LocalDate,
        note: String,
    ) {
        val transferId = Uuid.random().toString()
        val currentTime = DateUtils.currentInstant()

        val expenseOperation = Operation(
            id = Uuid.random().toString(),
            accountId = fromAccountId,
            type = OperationType.TRANSFER,
            amount = amount,
            category = TRANSFER_CATEGORY,
            date = date,
            note = note,
            createdAt = currentTime,
            linkedOperationId = transferId,
            toAccountId = toAccountId
        )

        val revenueOperation = Operation(
            id = Uuid.random().toString(),
            accountId = toAccountId,
            type = OperationType.TRANSFER,
            amount = amount,
            category = TRANSFER_CATEGORY,
            date = date,
            note = note,
            createdAt = currentTime,
            linkedOperationId = transferId,
            toAccountId = fromAccountId,
            isIncoming = true
        )

        operationRepository.addOperations(listOf(expenseOperation, revenueOperation))
        accountRepository.adjustBalance(fromAccountId, -amount)
        accountRepository.adjustBalance(toAccountId, amount)
    }

    private companion object {
        const val TRANSFER_CATEGORY = "Transfer"
    }
}
