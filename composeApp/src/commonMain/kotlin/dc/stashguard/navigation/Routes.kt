package dc.stashguard.navigation

import kotlinx.serialization.Serializable

@Serializable
object AccountsTab {
    const val ROUTE = "accounts_tab"
    const val TITLE = "Accounts"
}

@Serializable
object OperationsTab {
    const val ROUTE = "operations_tab"
    const val TITLE = "Operations"
}

// Nested Accounts routes
@Serializable
data class AccountDetails(val accountId: String)

@Serializable
object AddAccount