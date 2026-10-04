package dc.stashguard.feature.categories.di

import dc.stashguard.feature.categories.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    viewModel { CategoriesViewModel(get(), get()) }
}
