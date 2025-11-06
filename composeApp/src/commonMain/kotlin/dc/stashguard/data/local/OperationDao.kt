package dc.stashguard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperations(operations: List<OperationEntity>)

    // Read
    @Query("SELECT * FROM operations WHERE account_id = :accountId ORDER BY date DESC, created_at DESC")
    fun getOperationsByAccount(accountId: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE id = :operationId")
    suspend fun getOperationById(operationId: String): OperationEntity?

    @Query("SELECT * FROM operations WHERE account_id = :accountId AND type = :type ORDER BY date DESC")
    fun getOperationsByAccountAndType(accountId: String, type: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getOperationsByDateRange(startDate: String, endDate: String): Flow<List<OperationEntity>>

    // Update
    @Update
    suspend fun updateOperation(operation: OperationEntity)

    // Delete
    @Delete
    suspend fun deleteOperation(operation: OperationEntity)

    @Query("DELETE FROM operations WHERE id = :operationId")
    suspend fun deleteOperationById(operationId: String)

    @Query("DELETE FROM operations WHERE account_id = :accountId")
    suspend fun deleteOperationsByAccount(accountId: String)

    // Aggregations
    @Query("SELECT SUM(amount) FROM operations WHERE account_id = :accountId AND type = 'REVENUE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalRevenue(accountId: String, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM operations WHERE account_id = :accountId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenses(accountId: String, startDate: String, endDate: String): Double?

    // For transfers - get both sides of a transfer
    @Query("SELECT * FROM operations WHERE linked_operation_id = :linkedOperationId")
    suspend fun getLinkedOperations(linkedOperationId: String): List<OperationEntity>

    // If you need to get operations by date range for statistics
    @Query("SELECT * FROM operations WHERE account_id = :accountId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getOperationsByDateRange(accountId: String, startDate: String, endDate: String): Flow<List<OperationEntity>>
}