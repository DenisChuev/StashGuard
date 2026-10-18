package dc.stashguard.core.data.repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import dc.stashguard.core.database.AppDatabase
import dc.stashguard.core.domain.repository.TransactionRunner

/** DAO calls made inside [invoke] run on the transaction's connection, so they commit or roll back together. */
internal class RoomTransactionRunner(
    private val database: AppDatabase,
) : TransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R =
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { block() }
        }
}
