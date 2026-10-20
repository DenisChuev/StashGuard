package dc.stashguard.core.database.dao

import androidx.room.*
import dc.stashguard.core.database.entity.OperationEntity
import kotlinx.coroutines.flow.Flow

/** Query result: how much one day's operations moved an account's balance. */
data class DailyTotal(
    val date: String, // ISO date format: "2024-01-15"
    val total: Double
)

@Dao
interface OperationDao {

    // Create
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperations(operations: List<OperationEntity>)

    // Read
    @Query("SELECT * FROM operations")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations WHERE account_id = :accountId ORDER BY date DESC, created_at DESC LIMIT :limit")
    fun getOperationsByAccount(accountId: String, limit: Int): Flow<List<OperationEntity>>

    // Money in (revenue, receiving side of a transfer) counts positive, money out negative
    @Query(
        """
        SELECT date, SUM(CASE WHEN type = 'REVENUE' OR (type = 'TRANSFER' AND is_incoming = 1)
            THEN amount ELSE -amount END) AS total
        FROM operations WHERE account_id = :accountId GROUP BY date
        """
    )
    fun getDailyTotalsByAccount(accountId: String): Flow<List<DailyTotal>>

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

    @Query("DELETE FROM operations WHERE id IN (:operationIds)")
    suspend fun deleteOperationsByIds(operationIds: List<String>)

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

    // Transfers in other accounts whose other side is in this account
    @Query("SELECT * FROM operations WHERE type = 'TRANSFER' AND to_account_id = :accountId AND account_id != :accountId")
    suspend fun getTransferCounterparts(accountId: String): List<OperationEntity>

    // If you need to get operations by date range for statistics
    @Query("SELECT * FROM operations WHERE account_id = :accountId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getOperationsByDateRange(accountId: String, startDate: String, endDate: String): Flow<List<OperationEntity>>

    @Update
    suspend fun updateOperations(operations: List<OperationEntity>)
}