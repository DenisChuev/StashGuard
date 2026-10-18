package dc.stashguard.core.domain.repository

/**
 * Runs repository calls as one database transaction: either all of their writes are saved or,
 * if the block throws, none of them are.
 */
interface TransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}
