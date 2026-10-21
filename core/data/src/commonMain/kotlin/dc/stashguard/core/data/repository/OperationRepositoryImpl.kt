package dc.stashguard.core.data.repository

import dc.stashguard.core.data.mapper.toDomain
import dc.stashguard.core.data.mapper.toEntity
import dc.stashguard.core.database.dao.OperationDao
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

internal class OperationRepositoryImpl(
    private val operationDao: OperationDao,
) : OperationRepository {
    override fun observeOperations(): Flow<List<Operation>> =
        operationDao.getAllOperations().map { entities -> entities.map { it.toDomain() } }

    override fun observeOperationsByAccount(accountId: String, limit: Int): Flow<List<Operation>> =
        operationDao.getOperationsByAccount(accountId, limit).map { entities -> entities.map { it.toDomain() } }

    override fun observeDailyTotalsByAccount(accountId: String): Flow<Map<LocalDate, Double>> =
        operationDao.getDailyTotalsByAccount(accountId).map { totals ->
            totals.associate { LocalDate.parse(it.date) to it.total }
        }

    override fun observeOperationCountByCategory(categoryId: String): Flow<Int> =
        operationDao.observeOperationCountByCategory(categoryId)

    override suspend fun getOperation(id: String): Operation? =
        operationDao.getOperationById(id)?.toDomain()

    override suspend fun getLinkedOperations(linkedOperationId: String): List<Operation> =
        operationDao.getLinkedOperations(linkedOperationId).map { it.toDomain() }

    override suspend fun getTransferCounterparts(accountId: String): List<Operation> =
        operationDao.getTransferCounterparts(accountId).map { it.toDomain() }

    override suspend fun addOperations(operations: List<Operation>) {
        operationDao.insertOperations(operations.map { it.toEntity() })
    }

    override suspend fun updateOperations(operations: List<Operation>) {
        operationDao.updateOperations(operations.map { it.toEntity() })
    }

    override suspend fun deleteOperations(ids: List<String>) {
        operationDao.deleteOperationsByIds(ids)
    }

    override suspend fun deleteOperationsByAccount(accountId: String) {
        operationDao.deleteOperationsByAccount(accountId)
    }
}
