package dc.stashguard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dc.stashguard.data.local.CategoryEntity
import dc.stashguard.data.local.CategoryType

data class Category(
    val id: String,
    val name: String,
    val color: Color,
    val iconName: String,
    val type: CategoryType
)

fun CategoryEntity.toCategory(): Category {
    return Category(
        id = this.id,
        name = this.name,
        color = Color(this.color),
        iconName = this.iconName,
        type = this.type
    )
}

fun Category.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        name = this.name,
        color = this.color.toArgb(),
        iconName = this.iconName,
        type = this.type
    )
}