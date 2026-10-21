package dc.stashguard.core.domain.fake

import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** In-memory [OperationRepository] that behaves like the Room one. */
class FakeOperationRepository(vararg initial: Operation) : OperationRepository {
    private val operations = MutableStateFlow(initial.toList())

    /** Every operation, in insertion order. */
    val all: List<Operation> get() = operations.value

    fun get(id: String): Operation? = operations.value.firstOrNull { it.id == id }

    override fun observeOperations(): Flow<List<Operation>> = operations

    override fun observeOperationsByAccount(accountId: String, limit: Int): Flow<List<Operation>> =
        operations.map { list ->
            list.filter { it.accountId == accountId }
                .sortedWith(compareByDescending<Operation> { it.date }.thenByDescending { it.createdAt })
                .take(limit)
        }

    override fun observeDailyTotalsByAccount(accountId: String): Flow<Map<LocalDate, Double>> =
        operations.map { list ->
            list.filter { it.accountId == accountId }
                .groupBy { it.date }
                .mapValues { (_, dayOperations) -> dayOperations.sumOf { it.signedAmount() } }
        }

    override fun observeOperationCountByCategory(categoryId: String): Flow<Int> =
        operations.map { list -> list.count { it.category == categoryId } }

    override suspend fun getOperation(id: String): Operation? = get(id)

    override suspend fun getLinkedOperations(linkedOperationId: String): List<Operation> =
        operations.value.filter { it.linkedOperationId == linkedOperationId }

    override suspend fun getTransferCounterparts(accountId: String): List<Operation> =
        operations.value.filter {
            it.type == OperationType.TRANSFER && it.toAccountId == accountId && it.accountId != accountId
        }

    override suspend fun addOperations(operations: List<Operation>) {
        val ids = operations.map { it.id }.toSet()
        this.operations.update { list -> list.filterNot { it.id in ids } + operations }
    }

    override suspend fun updateOperations(operations: List<Operation>) {
        val byId = operations.associateBy { it.id }
        this.operations.update { list -> list.map { byId[it.id] ?: it } }
    }

    override suspend fun deleteOperations(ids: List<String>) {
        operations.update { list -> list.filterNot { it.id in ids } }
    }

    override suspend fun deleteOperationsByAccount(accountId: String) {
        operations.update { list -> list.filterNot { it.accountId == accountId } }
    }

    // Money in counts positive, as in OperationDao.getDailyTotalsByAccount
    private fun Operation.signedAmount(): Double = when (type) {
        OperationType.REVENUE -> amount
        OperationType.EXPENSE -> -amount
        OperationType.TRANSFER -> if (isIncoming) amount else -amount
    }
}
