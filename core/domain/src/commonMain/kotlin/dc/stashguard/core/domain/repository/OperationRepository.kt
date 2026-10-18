package dc.stashguard.core.domain.repository

import dc.stashguard.core.domain.model.Operation
import kotlinx.coroutines.flow.Flow

interface OperationRepository {
    fun observeOperations(): Flow<List<Operation>>

    /** Operations of one account, newest first. */
    fun observeOperationsByAccount(accountId: String): Flow<List<Operation>>

    suspend fun getOperation(id: String): Operation?

    /** Both sides of a transfer. */
    suspend fun getLinkedOperations(linkedOperationId: String): List<Operation>

    /** The other side of every transfer with [accountId]: transfers in other accounts pointing at it. */
    suspend fun getTransferCounterparts(accountId: String): List<Operation>

    suspend fun addOperations(operations: List<Operation>)

    suspend fun updateOperations(operations: List<Operation>)

    suspend fun deleteOperations(ids: List<String>)

    suspend fun deleteOperationsByAccount(accountId: String)
}
