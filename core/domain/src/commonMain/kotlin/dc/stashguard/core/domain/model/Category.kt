@file:OptIn(ExperimentalUuidApi::class)

package dc.stashguard.core.domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Id of the default "Transfer" category, which every transfer uses. */
const val TRANSFER_CATEGORY_ID = "category_transfer"

data class Category(
    val id: String = Uuid.random().toString(),
    val name: String,
    val colorArgb: Int,
    val iconName: String,
    val type: CategoryType
)

/** The built-in Transfer category: every transfer uses it, so it can't be edited or deleted. */
val Category.isTransfer: Boolean get() = id == TRANSFER_CATEGORY_ID

enum class CategoryType {
    REVENUE,
    EXPENSE,
    BOTH
}
