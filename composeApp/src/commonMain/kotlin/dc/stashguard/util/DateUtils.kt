@file:OptIn(ExperimentalTime::class)

package dc.stashguard.util

import co.touchlab.kermit.Logger
import dc.stashguard.util.DateUtils.currentDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val logger = Logger.withTag("DateUtils")

object DateUtils {
    /**
     * Gets current date
     */
    fun currentDate(): LocalDate = Clock.System.now().toLocalDate()

    /**
     * Gets current date's start as milliseconds (00:00:00 of today)
     */

    /**
     * Gets current instant (timezone-independent)
     */
    fun currentInstant(): Instant = Clock.System.now()

    /**
     * Gets current instant in system timezone as milliseconds
     */
    fun currentInstantMillis(): Long =
        Clock.System.now().toEpochMilliseconds()

    /**
     * Gets current date and time in system timezone as string
     */
    fun currentDateTimeString(): String =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()

    /**
     * Parses date string to LocalDate
     */
    fun parseDate(dateString: String): LocalDate = LocalDate.parse(dateString)

    /**
     * Formats LocalDate to ISO string
     */
    fun formatDate(localDate: LocalDate): String = localDate.toString()

    /**
     * Formats Instant to date string in system timezone
     */
    fun formatInstantToDate(instant: Instant): String =
        instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    /**
     * Formats Instant to date and time string in system timezone
     */
    fun formatInstantToDateTime(instant: Instant): String =
        instant.toLocalDateTime(TimeZone.currentSystemDefault()).toString()

    /**
     * Converts Instant to epoch seconds
     */
    fun instantToEpochSeconds(instant: Instant): Long = instant.epochSeconds

    /**
     * Converts epoch seconds to Instant
     */
    fun epochSecondsToInstant(epochSeconds: Long): Instant =
        Instant.fromEpochSeconds(epochSeconds)

    /**
     * Converts LocalDate to Instant (start of day in system timezone)
     */
    fun localDateToInstant(localDate: LocalDate): Instant =
        localDate.atStartOfDayIn(TimeZone.currentSystemDefault())

    /**
     * Converts LocalDate to milliseconds (start of day in system timezone)
     */
    fun localDateToMillis(localDate: LocalDate): Long =
        localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    /**
     * Converts milliseconds to LocalDate in system timezone
     */
    fun millisToLocalDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

    /**
     * Gets start of day for a given date in system timezone as milliseconds
     */
    fun startOfDayMillis(localDate: LocalDate): Long =
        localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    /**
     * Gets end of day for a given date in system timezone as milliseconds
     */
    fun endOfDayMillis(localDate: LocalDate): Long =
        localDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds() - 1

    /**
     * Checks if two dates are the same (ignoring timezone)
     */
    fun isSameDate(date1: LocalDate, date2: LocalDate): Boolean =
        date1 == date2

    /**
     * Gets days difference between two dates
     */
    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Long =
        (endDate.toEpochDays() - startDate.toEpochDays())

    /**
     * Formats LocalDate to human-readable string (e.g., "Apr 16, 2024")
     */
    fun formatDateReadable(localDate: LocalDate): String {
        return localDate.format(
            LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth()
                chars(", ")
                year()
            }
        )
    }

    /**
     * Formats LocalDate to short format (e.g., "04/16/2024")
     */
    fun formatDateShort(localDate: LocalDate): String {
        return localDate.format(
            LocalDate.Format {
                monthNumber()
                char('/')
                dayOfMonth()
                char('/')
                year()
            }
        )
    }

    /**
     * Formats Instant to readable date string (e.g., "Apr 16, 2024")
     */
    fun formatInstantToReadableDate(instant: Instant): String {
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return formatDateReadable(localDate)
    }

    /**
     * Formats Instant to readable date and time (e.g., "Apr 16, 2024, 14:30")
     */
    fun formatInstantToReadableDateTime(instant: Instant): String {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.format(
            LocalDateTime.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(" ")
                dayOfMonth()
                chars(", ")
                year()
                chars(", ")
                hour()
                char(':')
                minute()
            }
        )
    }

    /**
     * Formats date for operations display (e.g., "Today", "Yesterday", "Apr 16")
     */
    fun formatDateForOperations(localDate: LocalDate): String {
        val today = currentDate()
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        return when {
            localDate == today -> "Today"
            localDate == yesterday -> "Yesterday"
            localDate.year == today.year -> formatDateMonthDay(localDate) // "Apr 16"
            else -> formatDateReadable(localDate) // "Apr 16, 2023"
        }
    }

    /**
     * Formats just month and day (e.g., "Apr 16")
     */
    private fun formatDateMonthDay(localDate: LocalDate): String {
        return localDate.format(
            LocalDate.Format {
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                chars(" ")
                day()
            }
        )
    }

    /**
     * Formats relative time for recent operations (e.g., "2h ago", "1d ago")
     */
    fun formatRelativeTime(instant: Instant): String {
        val now = Clock.System.now()
        val duration = now - instant

        logger.d { "now: $now, instant: $instant, diff: $duration" }
        return when {
            duration.inWholeSeconds < 60 -> "Just now"
            duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
            duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
            duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
            else -> formatInstantToReadableDate(instant)
        }
    }
}

// Extension functions for easier usage
fun LocalDate.toMillis(): Long = DateUtils.localDateToMillis(this)
fun Instant.toLocalDate(): LocalDate =
    DateUtils.formatInstantToDate(this).let { LocalDate.parse(it) }

fun Long.toLocalDate(): LocalDate = DateUtils.millisToLocalDate(this)
fun LocalDate.formatReadable(): String = DateUtils.formatDateReadable(this)
fun LocalDate.formatShort(): String = DateUtils.formatDateShort(this)
fun LocalDate.formatForOperations(): String = DateUtils.formatDateForOperations(this)

fun Instant.formatReadableDate(): String = DateUtils.formatInstantToReadableDate(this)
fun Instant.formatReadableDateTime(): String = DateUtils.formatInstantToReadableDateTime(this)
fun Instant.formatRelativeTime(): String = DateUtils.formatRelativeTime(this)