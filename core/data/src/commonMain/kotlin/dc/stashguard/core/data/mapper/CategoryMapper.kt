package dc.stashguard.core.data.mapper

import dc.stashguard.core.database.entity.CategoryEntity
import dc.stashguard.core.domain.model.Category
import dc.stashguard.core.domain.model.CategoryType

internal fun CategoryEntity.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        colorArgb = this.color,
        iconName = this.iconName,
        type = CategoryType.valueOf(this.type)
    )
}

internal fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        name = this.name,
        color = this.colorArgb,
        iconName = this.iconName,
        type = this.type.name
    )
}
