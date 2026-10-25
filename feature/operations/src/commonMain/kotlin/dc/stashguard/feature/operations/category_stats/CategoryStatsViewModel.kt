package dc.stashguard.feature.operations.category_stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dc.stashguard.core.common.DateUtils
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth

data class CategoryStatsState(
    val type: OperationType,
    val month: YearMonth,
    /** False on the current month: there is nothing recorded in the future to step to. */
    val canGoNext: Boolean,
    val total: Double,
    /** Biggest first. Empty when nothing of [type] was recorded in [month]. */
    val slices: List<CategorySlice>
)

/**
 * A month's income or expenses split by category, for the donut chart. Opens on [currentMonth]
 * with [initialType] (revenue or expense); the screen switches type and steps through months.
 */
class CategoryStatsViewModel(
    operationRepository: OperationRepository,
    categoryRepository: CategoryRepository,
    initialType: OperationType,
    private val currentMonth: YearMonth = DateUtils.currentDate().yearMonth,
) : ViewModel() {
    private val type = MutableStateFlow(initialType)
    private val month = MutableStateFlow(currentMonth)

    /** Null until the first database read, so the empty state doesn't flash while loading. */
    val state: StateFlow<CategoryStatsState?> = combine(
        operationRepository.observeOperations(),
        categoryRepository.observeCategories(),
        type,
        month
    ) { operations, categories, type, month ->
        val slices = operations.filter { it.date.yearMonth == month }
            .categorySlices(categories.associateBy { it.id }, type)
        CategoryStatsState(
            type = type,
            month = month,
            canGoNext = month < currentMonth,
            total = slices.sumOf { it.amount },
            slices = slices
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectType(type: OperationType) {
        this.type.value = type
    }

    fun previousMonth() {
        month.update { it.minusMonth() }
    }

    fun nextMonth() {
        month.update { if (it < currentMonth) it.plusMonth() else it }
    }
}
