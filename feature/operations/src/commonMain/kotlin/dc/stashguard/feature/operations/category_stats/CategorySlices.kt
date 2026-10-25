package dc.stashguard.feature.operations.category_stats

import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.Operation
import dc.stashguard.core.domain.model.OperationType

/** One category's share of the income or expenses. [category] is null for uncategorized operations. */
data class CategorySlice(
    val category: Category?,
    val amount: Double,
    val fraction: Float
)

/** The [type] operations summed per category, biggest first. Transfers never match a revenue or expense type. */
internal fun List<Operation>.categorySlices(
    categories: Map<String, Category>,
    type: OperationType
): List<CategorySlice> {
    // Operations whose category was deleted share one Uncategorized slice
    val amounts = filter { it.type == type }
        .groupBy { categories[it.category] }
        .mapValues { (_, operations) -> operations.sumOf { it.amount } }
    val total = amounts.values.sum()
    return amounts
        .map { (category, amount) -> CategorySlice(category, amount, (amount / total).toFloat()) }
        .sortedByDescending { it.amount }
}
