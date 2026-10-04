package dc.stashguard.core.data.di

import dc.stashguard.core.data.repository.AccountRepositoryImpl
import dc.stashguard.core.data.repository.CategoryRepositoryImpl
import dc.stashguard.core.data.repository.OperationRepositoryImpl
import dc.stashguard.core.domain.repository.AccountRepository
import dc.stashguard.core.domain.repository.CategoryRepository
import dc.stashguard.core.domain.repository.OperationRepository
import org.koin.dsl.module

val dataModule = module {
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    single<OperationRepository> { OperationRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
}
