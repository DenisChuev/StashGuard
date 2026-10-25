package dc.stashguard.feature.operations.di

import dc.stashguard.core.domain.model.OperationType
import dc.stashguard.feature.operations.OperationsViewModel
import dc.stashguard.feature.operations.add_operation.AddOperationViewModel
import dc.stashguard.feature.operations.category_stats.CategoryStatsViewModel
import dc.stashguard.feature.operations.edit_operation.EditOperationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val operationsModule = module {
    viewModel { OperationsViewModel(get(), get(), get()) }

    viewModel { (operationType: OperationType) ->
        CategoryStatsViewModel(
            operationRepository = get(),
            categoryRepository = get(),
            initialType = operationType
        )
    }

    viewModel { (accountId: String, operationType: OperationType) ->
        AddOperationViewModel(
            accountRepository = get(),
            categoryRepository = get(),
            addOperationUseCase = get(),
            accountId = accountId,
            operationType = operationType
        )
    }

    viewModel { (operationId: String) ->
        EditOperationViewModel(
            accountRepository = get(),
            categoryRepository = get(),
            operationRepository = get(),
            updateOperationUseCase = get(),
            deleteOperationUseCase = get(),
            operationId = operationId
        )
    }
}
