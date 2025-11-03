package dc.stashguard.navigation

import kotlinx.serialization.Serializable

@Serializable
object Accounts

@Serializable
object Operations

@Serializable
data class AccountDetails(val accountId: String)

@Serializable
object AddAccount