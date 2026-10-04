package dc.stashguard.navigation

import androidx.navigation3.runtime.NavKey
import dc.stashguard.core.domain.model.OperationType
import kotlinx.serialization.Serializable

// Tabs
@Serializable
data object AccountsTab : NavKey

@Serializable
data object OperationsTab : NavKey

@Serializable
data object CategoriesTab : NavKey

// Nested Accounts routes
@Serializable
data class EditAccount(val accountId: String) : NavKey

@Serializable
data object AddAccount : NavKey

@Serializable
data class DetailsAccount(val accountId: String) : NavKey

// Nested Operations routes
@Serializable
data class AddOperation(
    val accountId: String,
    val operationType: OperationType
) : NavKey

@Serializable
data class EditOperation(
    val accountId: String,
    val operationId: String,
    val operationType: OperationType
) : NavKey
