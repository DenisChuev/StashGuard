package dc.stashguard.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    // Query to get all accounts, returning a Flow for reactive updates
    @Query("SELECT * FROM accounts ORDER BY name COLLATE NOCASE ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    // Query to get all accounts synchronously
    @Query("SELECT * FROM accounts ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllAccountsSuspend(): List<AccountEntity>

    // Query to get a specific account by ID
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    // Insert a single account
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    // Insert multiple accounts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    // Update an existing account
    @Update
    suspend fun updateAccount(account: AccountEntity)

    // Delete a specific account
    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    // Delete accounts by ID (alternative to @Delete)
    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: String)

    // Delete all accounts
    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()
}