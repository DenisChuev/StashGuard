package dc.stashguard.di

import android.content.Context
import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.feature.accounts.details.DetailsAccountViewModel
import dc.stashguard.feature.accounts.edit_account.EditAccountViewModel
import dc.stashguard.feature.categories.edit_category.EditCategoryViewModel
import dc.stashguard.feature.operations.add_operation.AddOperationViewModel
import dc.stashguard.feature.operations.category_stats.CategoryStatsViewModel
import dc.stashguard.feature.operations.edit_operation.EditOperationViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Checks that every constructor dependency in the Koin graph has a definition, so a forgotten
 * registration (a new use case or ViewModel) fails CI instead of crashing when a screen opens.
 * Nothing is created: Koin inspects constructors by reflection, which is why this runs on the JVM only.
 * Only a definition's declared type is inspected, so implementations bound to an interface
 * (the repositories) don't have their own constructors checked.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinGraphTest {
    @Test
    fun everyDependencyHasADefinition() {
        module { includes(appModules) }.verify(
            // Provided by androidContext() in StashGuardApp
            extraTypes = listOf(Context::class),
            // Passed by the screens with parametersOf(...)
            injections = injectedParameters(
                definition<EditAccountViewModel>(String::class),
                definition<DetailsAccountViewModel>(String::class),
                definition<AddOperationViewModel>(String::class, OperationType::class),
                definition<EditOperationViewModel>(String::class),
                definition<CategoryStatsViewModel>(OperationType::class),
                definition<EditCategoryViewModel>(String::class),
            ),
        )
    }
}
