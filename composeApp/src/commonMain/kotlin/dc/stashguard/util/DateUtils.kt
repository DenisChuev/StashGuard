@file:OptIn(ExperimentalTime::class)

package dc.stashguard.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object DateUtils {

    /**
     * Gets current date in the system's timezone
     */
    fun currentDate(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    /**
     * Gets current date's start as milliseconds (00:00:00 of today in system timezone)
     */
    fun currentDateMillis(): Long =
        currentDate().atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

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
}

// Extension functions for easier usage
fun LocalDate.toMillis(): Long = DateUtils.localDateToMillis(this)
fun Instant.toLocalDate(): LocalDate =
    DateUtils.formatInstantToDate(this).let { LocalDate.parse(it) }

fun Long.toLocalDate(): LocalDate = DateUtils.millisToLocalDate(this)