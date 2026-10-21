@file:OptIn(ExperimentalTime::class)

package dc.stashguard.feature.operations

import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.testing.FakeAccountRepository
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.TEST_DATE
import dc.stashguard.core.testing.account
import dc.stashguard.core.testing.keepCollecting
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class OperationsViewModelTest : MainDispatcherTest() {
    private val operations = FakeOperationRepository()

    private fun viewModel() = OperationsViewModel(
        operationRepository = operations,
        categoryRepository = FakeCategoryRepository(),
        accountRepository = FakeAccountRepository(account("a"), account("b")),
    )

    @Test
    fun newestDateComesFirstThenTheLatestAddedThatDay() = runTest {
        val dayBefore = TEST_DATE.minus(1, DateTimeUnit.DAY)
        operations.addOperations(
            listOf(
                // Added last, but for the day before
                operation("backdated", "a", OperationType.EXPENSE, 1.0)
                    .copy(date = dayBefore, createdAt = Instant.fromEpochMilliseconds(3)),
                operation("first", "a", OperationType.EXPENSE, 1.0).copy(createdAt = Instant.fromEpochMilliseconds(1)),
                operation("second", "a", OperationType.EXPENSE, 1.0).copy(createdAt = Instant.fromEpochMilliseconds(2)),
            )
        )
        val viewModel = viewModel()
        keepCollecting(viewModel.operations)

        assertEquals(listOf("second", "first", "backdated"), viewModel.operations.value?.map { it.id })
    }

    @Test
    fun aTransferIsListedOnceBySendingSide() = runTest {
        operations.addOperations(transfer("a", "b", 100.0).toList())
        val viewModel = viewModel()
        keepCollecting(viewModel.operations)

        assertEquals(listOf("transfer-out"), viewModel.operations.value?.map { it.id })
    }
}
