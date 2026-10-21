package dc.stashguard.core.ui

import dc.stashguard.core.testing.transfer
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferAccountsTest {
    private val names = mapOf("card" to "Card", "cash" to "Cash")
    private val sides = transfer(fromAccountId = "card", toAccountId = "cash", amount = 100.0)

    @Test
    fun bothSidesReadFromSenderToReceiver() {
        val (sending, receiving) = sides

        assertEquals("Card" to "Cash", transferAccounts(sending, names))
        assertEquals("Card" to "Cash", transferAccounts(receiving, names))
    }

    @Test
    fun aDeletedAccountIsNamedAsSuch() {
        val (sending, receiving) = sides

        assertEquals("Deleted account" to "Cash", transferAccounts(receiving, names - "card"))
        assertEquals("Card" to "Deleted account", transferAccounts(sending, names - "cash"))
    }
}
