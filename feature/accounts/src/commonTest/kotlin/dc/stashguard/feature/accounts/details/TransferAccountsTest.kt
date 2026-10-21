package dc.stashguard.feature.accounts.details

import dc.stashguard.core.testing.transfer
import kotlin.test.Test
import kotlin.test.assertEquals

class TransferAccountsTest {
    private val names = mapOf("card" to "Card", "cash" to "Cash")
    private val sides = transfer(fromAccountId = "card", toAccountId = "cash", amount = 100.0)

    @Test
    fun bothSidesReadFromSenderToReceiver() {
        val (sending, receiving) = sides

        assertEquals("Card" to "Cash", transferAccounts(sending, "Card", names))
        assertEquals("Card" to "Cash", transferAccounts(receiving, "Cash", names))
    }

    @Test
    fun aDeletedOtherAccountIsNamedAsSuch() {
        val (_, receiving) = sides

        assertEquals("Deleted account" to "Cash", transferAccounts(receiving, "Cash", names - "card"))
    }
}
