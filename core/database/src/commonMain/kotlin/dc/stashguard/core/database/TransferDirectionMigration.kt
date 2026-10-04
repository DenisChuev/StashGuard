package dc.stashguard.core.database

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Version 5 adds `operations.is_incoming`. Before it, the two sides of a transfer were
 * indistinguishable, but the sending side was always inserted first, so in each linked
 * pair the row with the higher rowid is the receiving side.
 */
internal class TransferDirectionMigration : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            UPDATE operations SET is_incoming = 1
            WHERE type = 'TRANSFER'
              AND linked_operation_id IS NOT NULL
              AND rowid > (
                  SELECT MIN(sender.rowid) FROM operations AS sender
                  WHERE sender.linked_operation_id = operations.linked_operation_id
              )
            """.trimIndent()
        )
    }
}
