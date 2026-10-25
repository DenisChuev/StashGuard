package dc.stashguard.feature.operations.category_stats

import dc.stashguard.core.domain.model.CategoryType
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.testing.FakeCategoryRepository
import dc.stashguard.core.testing.FakeOperationRepository
import dc.stashguard.core.testing.MainDispatcherTest
import dc.stashguard.core.testing.TEST_DATE
import dc.stashguard.core.testing.category
import dc.stashguard.core.testing.keepCollecting
import dc.stashguard.core.testing.operation
import dc.stashguard.core.testing.transfer
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CategoryStatsViewModelTest : MainDispatcherTest() {
    private val operations = FakeOperationRepository()
    private val categories = FakeCategoryRepository(
        category("food"),
        category("rent"),
        category("salary", CategoryType.REVENUE),
    )

    private fun viewModel(type: OperationType = OperationType.EXPENSE) = CategoryStatsViewModel(
        operationRepository = operations,
        categoryRepository = categories,
        initialType = type,
        currentMonth = TEST_DATE.yearMonth,
    )

    @Test
    fun amountsAreSummedPerCategoryBiggestFirst() = runTest {
        operations.addOperations(
            listOf(
                operation("food1", "a", OperationType.EXPENSE, 10.0, category = "food"),
                operation("rent", "a", OperationType.EXPENSE, 50.0, category = "rent"),
                operation("food2", "a", OperationType.EXPENSE, 15.0, category = "food"),
            )
        )
        val viewModel = viewModel()
        keepCollecting(viewModel.state)

        val state = viewModel.state.value!!
        assertEquals(listOf("rent", "food"), state.slices.map { it.category?.id })
        assertEquals(listOf(50.0, 25.0), state.slices.map { it.amount })
        assertEquals(75.0, state.total)
        assertEquals(listOf(2f / 3, 1f / 3), state.slices.map { it.fraction })
    }

    @Test
    fun onlyTheSelectedTypeCountsAndTransfersNever() = runTest {
        operations.addOperations(
            transfer("a", "b", 100.0).toList() + listOf(
                operation("expense", "a", OperationType.EXPENSE, 10.0, category = "food"),
                operation("salary", "a", OperationType.REVENUE, 500.0, category = "salary"),
            )
        )
        val viewModel = viewModel(OperationType.REVENUE)
        keepCollecting(viewModel.state)

        assertEquals(listOf("salary"), viewModel.state.value?.slices?.map { it.category?.id })

        viewModel.selectType(OperationType.EXPENSE)

        assertEquals(listOf("food"), viewModel.state.value?.slices?.map { it.category?.id })
    }

    @Test
    fun onlyTheSelectedMonthCounts() = runTest {
        val lastMonth = TEST_DATE.minus(1, DateTimeUnit.MONTH)
        operations.addOperations(
            listOf(
                operation("now", "a", OperationType.EXPENSE, 10.0, category = "food"),
                operation("before", "a", OperationType.EXPENSE, 30.0, category = "rent").copy(date = lastMonth),
            )
        )
        val viewModel = viewModel()
        keepCollecting(viewModel.state)

        assertEquals(10.0, viewModel.state.value?.total)
        assertFalse(viewModel.state.value!!.canGoNext)

        viewModel.previousMonth()

        assertEquals(lastMonth.yearMonth, viewModel.state.value?.month)
        assertEquals(30.0, viewModel.state.value?.total)
        assertTrue(viewModel.state.value!!.canGoNext)

        viewModel.nextMonth()
        // Already on the current month: there is no later one to step to
        viewModel.nextMonth()

        assertEquals(TEST_DATE.yearMonth, viewModel.state.value?.month)
    }

    @Test
    fun operationsWithoutAKnownCategoryShareOneSlice() = runTest {
        operations.addOperations(
            listOf(
                operation("deleted1", "a", OperationType.EXPENSE, 10.0, category = "deleted"),
                operation("deleted2", "a", OperationType.EXPENSE, 5.0, category = "gone"),
            )
        )
        val viewModel = viewModel()
        keepCollecting(viewModel.state)

        val slices = viewModel.state.value!!.slices
        assertEquals(1, slices.size)
        assertEquals(null, slices.single().category)
        assertEquals(15.0, slices.single().amount)
    }

    @Test
    fun aMonthWithNothingRecordedHasNoSlices() = runTest {
        val viewModel = viewModel()
        keepCollecting(viewModel.state)

        assertEquals(emptyList(), viewModel.state.value?.slices)
        assertEquals(0.0, viewModel.state.value?.total)
    }
}
