@file:OptIn(ExperimentalTime::class)

package dc.stashguard.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object DateUtils {
    fun currentDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun currentInstant(): Instant {
        return Clock.System.now()
    }

    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    fun formatDate(localDate: LocalDate): String {
        return localDate.toString() // ISO format: "2024-01-15"
    }

    fun instantToEpochSeconds(instant: Instant): Long {
        return instant.epochSeconds
    }

    fun epochSecondsToInstant(epochSeconds: Long): Instant {
        return Instant.fromEpochSeconds(epochSeconds)
    }
}