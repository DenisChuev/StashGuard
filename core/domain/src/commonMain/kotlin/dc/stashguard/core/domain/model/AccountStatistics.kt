package dc.stashguard.core.domain.model

data class AccountStatistics(
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netChange: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTransaction: Double = 0.0
)
