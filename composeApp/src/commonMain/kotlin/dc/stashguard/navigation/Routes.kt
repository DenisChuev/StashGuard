package dc.stashguard.navigation

import dc.stashguard.model.OperationType
import kotlinx.serialization.Serializable

@Serializable
object AccountsTab {
    const val ROUTE = "dc.stashguard.navigation.AccountsTab"
    const val TITLE = "Accounts"
}

@Serializable
object OperationsTab {
    const val ROUTE = "dc.stashguard.navigation.OperationsTab"
    const val TITLE = "Operations"
}

// Nested Accounts routes
@Serializable
data class EditAccount(val accountId: String)

@Serializable
object AddAccount

@Serializable
data class DetailsAccount(val accountId: String)

@Serializable
data class AddOperation(
    val accountId: String,
    val operationType: OperationType
)