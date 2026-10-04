package dc.stashguard.di

import dc.stashguard.core.data.di.dataModule
import dc.stashguard.core.database.di.databaseModule
import dc.stashguard.core.domain.usecase.AddAccountUseCase
import dc.stashguard.core.domain.usecase.AddOperationUseCase
import dc.stashguard.core.domain.usecase.CalculateAccountStatisticsUseCase
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import dc.stashguard.core.domain.usecase.InitializeDefaultCategoriesUseCase
import dc.stashguard.core.domain.usecase.UpdateOperationUseCase
import dc.stashguard.feature.accounts.di.accountsModule
import dc.stashguard.feature.categories.di.categoriesModule
import dc.stashguard.feature.operations.di.operationsModule
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// :core:domain is kept free of Koin, so its use cases are registered here.
val domainModule = module {
    factoryOf(::AddAccountUseCase)
    factoryOf(::DeleteAccountUseCase)
    factoryOf(::CalculateAccountStatisticsUseCase)
    factoryOf(::AddOperationUseCase)
    factoryOf(::UpdateOperationUseCase)
    factoryOf(::InitializeDefaultCategoriesUseCase)
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            databaseModule,
            dataModule,
            domainModule,
            accountsModule,
            operationsModule,
            categoriesModule,
        )
    }
}
