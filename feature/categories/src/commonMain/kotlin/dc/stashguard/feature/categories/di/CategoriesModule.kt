package dc.stashguard.feature.categories.di

import dc.stashguard.feature.categories.add_category.AddCategoryViewModel
import dc.stashguard.feature.categories.categories_list.CategoriesViewModel
import dc.stashguard.feature.categories.edit_category.EditCategoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    viewModel { CategoriesViewModel(get()) }
    viewModel { AddCategoryViewModel(get()) }
    viewModel { (categoryId: String) -> EditCategoryViewModel(get(), get(), get(), categoryId) }
}
