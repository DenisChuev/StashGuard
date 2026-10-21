package dc.stashguard.core.domain.fake

import dc.stashguard.core.domain.repository.TransactionRunner

/**
 * Runs the block directly and counts the transactions. Rollback on failure is Room's job and is
 * not simulated: use case tests check that invalid input is rejected before anything is written.
 */
class FakeTransactionRunner : TransactionRunner {
    var transactionCount = 0
        private set

    override suspend fun <R> invoke(block: suspend () -> R): R {
        transactionCount++
        return block()
    }
}
