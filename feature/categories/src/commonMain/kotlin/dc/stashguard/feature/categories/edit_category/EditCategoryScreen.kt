@file:OptIn(ExperimentalMaterial3Api::class)

package dc.stashguard.feature.categories.edit_category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dc.stashguard.core.domain.model.isTransfer
import dc.stashguard.feature.categories.add_category.CategoryForm
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditCategoryScreen(
    categoryId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditCategoryViewModel = koinViewModel { parametersOf(categoryId) }
) {
    val category by viewModel.category.collectAsState()
    val form by viewModel.form.collectAsState()
    val operationCount by viewModel.operationCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // The categories list doesn't open the Transfer category, so this is only a safeguard
    val loadedCategory = category
    if (loadedCategory == null || loadedCategory.isTransfer) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = error ?: "Category not found.")
        }
        return
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        if (operationCount > 0) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Can't delete category") },
                text = {
                    Text(
                        "\"${loadedCategory.name}\" is used by ${operationCountText(operationCount)}. " +
                            "Move them to another category first."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("OK")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete category?") },
                text = { Text("\"${loadedCategory.name}\" will be deleted. This can't be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            viewModel.deleteCategory(onSuccess = onNavigateBack)
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryForm(
                form = form,
                error = error,
                onNameChange = viewModel::updateName,
                onTypeChange = viewModel::updateType,
                onColorChange = viewModel::updateColor,
                onIconChange = viewModel::updateIcon,
                typeLockedReason = if (operationCount > 0) {
                    "Used by ${operationCountText(operationCount)}, so the type can't change."
                } else {
                    null
                },
                modifier = Modifier.weight(1f)
            )

            // Same look as the Add Category button
            Button(
                onClick = { viewModel.saveEditedCategory(onSuccess = onNavigateBack) },
                enabled = !isSaving,
                shape = MaterialTheme.shapes.medium,
                // As tall as the text field above
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save")
            }
        }
    }
}
