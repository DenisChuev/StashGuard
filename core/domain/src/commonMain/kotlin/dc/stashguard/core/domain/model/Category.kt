package dc.stashguard.core.domain.model

/** Id of the default "Transfer" category, which every transfer uses. */
const val TRANSFER_CATEGORY_ID = "category_transfer"

data class Category(
    val id: String,
    val name: String,
    val colorArgb: Int,
    val iconName: String,
    val type: CategoryType
)

enum class CategoryType {
    REVENUE,
    EXPENSE,
    BOTH
}
