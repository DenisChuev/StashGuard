package dc.stashguard.core.domain.usecase

import dc.stashguard.core.common.DateUtils
import dc.stashguard.core.domain.model.AccountStatistics
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType
import kotlinx.datetime.LocalDate

/**
 * Revenue and expense totals over the last 30 days. Transfers are neither, but they count in
 * the net change, so it matches how much the balance moved.
 */
class CalculateAccountStatisticsUseCase {
    operator fun invoke(
        operations: List<Operation>,
        today: LocalDate = DateUtils.currentDate(),
    ): AccountStatistics {
        val last30DaysOperations = operations.filter { operation ->
            DateUtils.daysBetween(operation.date, today) <= 30
        }

        val totalRevenue = last30DaysOperations
            .filter { it.type == OperationType.REVENUE }
            .sumOf { it.amount }

        val totalExpenses = last30DaysOperations
            .filter { it.type == OperationType.EXPENSE }
            .sumOf { it.amount }

        val (transfersIn, transfersOut) = last30DaysOperations
            .filter { it.type == OperationType.TRANSFER }
            .partition { it.isIncoming }

        val netChange = totalRevenue - totalExpenses +
            transfersIn.sumOf { it.amount } - transfersOut.sumOf { it.amount }

        return AccountStatistics(
            totalRevenue = totalRevenue,
            totalExpenses = totalExpenses,
            netChange = netChange,
            transactionCount = last30DaysOperations.size,
            averageTransaction = if (last30DaysOperations.isNotEmpty()) {
                (totalRevenue + totalExpenses) / last30DaysOperations.size
            } else 0.0
        )
    }
}
