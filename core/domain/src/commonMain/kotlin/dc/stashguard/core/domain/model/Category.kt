package dc.stashguard.core.domain.model

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
