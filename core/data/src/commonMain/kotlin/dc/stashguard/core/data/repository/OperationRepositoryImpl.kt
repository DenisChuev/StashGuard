package dc.stashguard.core.data.repository

import dc.stashguard.core.data.mapper.toDomain
import dc.stashguard.core.data.mapper.toEntity
import dc.stashguard.core.database.dao.OperationDao
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OperationRepositoryImpl(
    private val operationDao: OperationDao,
) : OperationRepository {
    override fun observeOperations(): Flow<List<Operation>> =
        operationDao.getAllOperations().map { entities -> entities.map { it.toDomain() } }

    override fun observeOperationsByAccount(accountId: String): Flow<List<Operation>> =
        operationDao.getOperationsByAccount(accountId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getOperation(id: String): Operation? =
        operationDao.getOperationById(id)?.toDomain()

    override suspend fun getLinkedOperations(linkedOperationId: String): List<Operation> =
        operationDao.getLinkedOperations(linkedOperationId).map { it.toDomain() }

    override suspend fun addOperations(operations: List<Operation>) {
        operationDao.insertOperations(operations.map { it.toEntity() })
    }

    override suspend fun updateOperations(operations: List<Operation>) {
        operationDao.updateOperations(operations.map { it.toEntity() })
    }

    override suspend fun deleteOperationsByAccount(accountId: String) {
        operationDao.deleteOperationsByAccount(accountId)
    }
}
