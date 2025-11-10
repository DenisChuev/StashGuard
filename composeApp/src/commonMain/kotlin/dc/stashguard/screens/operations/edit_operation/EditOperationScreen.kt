package dc.stashguard.screens.operations.edit_operation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dc.stashguard.model.OperationType
import dc.stashguard.screens.operations.add_operation.AddOperationContent
import dc.stashguard.screens.operations.add_operation.OperationTopAppBar
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.collections.emptyList

@Composable
fun EditOperationScreen(
    operationId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditOperationViewModel = koinViewModel { parametersOf(operationId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val availableAccounts by viewModel.availableAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    val operationType by derivedStateOf {
        viewModel.getOperationType() ?: OperationType.EXPENSE // Placeholder
    }

    Scaffold(
        topBar = {
            OperationTopAppBar(
                operationType = operationType,
                onNavigateBack = onNavigateBack
            )
        },
        content = { paddingValues ->
            AddOperationContent(
                state = state,
                operationType = operationType,
                availableAccounts = availableAccounts,
                availableCategories = availableCategories,
                onAmountChange = viewModel::updateAmount,
                onCategoryChange = viewModel::updateCategory,
                onToAccountChange = viewModel::updateToAccount,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSave = { viewModel.saveOperation(onNavigateBack) },
                modifier = Modifier.padding(paddingValues),
            )
        }
    )
}
