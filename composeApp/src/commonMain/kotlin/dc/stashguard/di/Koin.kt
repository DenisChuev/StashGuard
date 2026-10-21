package dc.stashguard.di

import co.touchlab.kermit.Logger
import dc.stashguard.core.data.di.dataModule
import dc.stashguard.core.database.di.databaseModule
import dc.stashguard.core.domain.usecase.AddAccountUseCase
import dc.stashguard.core.domain.usecase.AddOperationUseCase
import dc.stashguard.core.domain.usecase.DeleteAccountUseCase
import dc.stashguard.core.domain.usecase.DeleteCategoryUseCase
import dc.stashguard.core.domain.usecase.DeleteOperationUseCase
import dc.stashguard.core.domain.usecase.InitializeDefaultCategoriesUseCase
import dc.stashguard.core.domain.usecase.UpdateOperationUseCase
import dc.stashguard.feature.accounts.di.accountsModule
import dc.stashguard.feature.categories.di.categoriesModule
import dc.stashguard.feature.operations.di.operationsModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// :core:domain is kept free of Koin, so its use cases are registered here.
val domainModule = module {
    factoryOf(::AddAccountUseCase)
    factoryOf(::DeleteAccountUseCase)
    factoryOf(::AddOperationUseCase)
    factoryOf(::UpdateOperationUseCase)
    factoryOf(::DeleteOperationUseCase)
    factoryOf(::InitializeDefaultCategoriesUseCase)
    factoryOf(::DeleteCategoryUseCase)
}

private val logger = Logger.withTag("Koin")

// Lives as long as the app process, for startup work that isn't tied to a screen
private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** Every module the app starts with; KoinGraphTest verifies the same list. */
val appModules = listOf(
    databaseModule,
    dataModule,
    domainModule,
    accountsModule,
    operationsModule,
    categoriesModule,
)

/** Starts Koin, then seeds the default categories. Called once at app startup. */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    val koin = startKoin {
        appDeclaration()
        modules(appModules)
    }.koin

    seedDefaultCategories(koin)
}

/** Creates the default categories on the first launch, before any screen needs them. */
private fun seedDefaultCategories(koin: Koin) {
    appScope.launch {
        try {
            koin.get<InitializeDefaultCategoriesUseCase>()()
        } catch (e: Exception) {
            logger.e(e) { "Error seeding the default categories" }
        }
    }
}
