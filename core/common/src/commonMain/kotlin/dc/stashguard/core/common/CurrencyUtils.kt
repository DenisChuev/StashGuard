package dc.stashguard.core.common

import kotlin.math.pow

/** Currency shown next to every amount. Fixed to rubles until currencies are configurable. */
const val CURRENCY_SYMBOL = "₽"

/** [toBalanceString] followed by [CURRENCY_SYMBOL], e.g. "1 234.5 ₽". */
fun Double.toCurrencyString(): String = "${toBalanceString()} $CURRENCY_SYMBOL"

fun String.toBalanceDouble() = this.replace(" ", "").toDoubleOrNull()

fun Double.toBalanceString(maxFractionDigits: Int = 2): String {
    // Round once in minor units: avoids floating garbage, lets 1.999 carry to 2,
    // and keeps the sign of values between -1 and 0
    val multiplier = 10.0.pow(maxFractionDigits).toLong()
    val minorUnits = kotlin.math.round(kotlin.math.abs(this) * multiplier).toLong()
    if (minorUnits == 0L) return "0"

    val sign = if (this < 0) "-" else ""
    val integerPart = minorUnits / multiplier
    val fracStr = (minorUnits % multiplier).toString()
        .padStart(maxFractionDigits, '0')
        .trimEnd('0')

    val number = if (fracStr.isEmpty()) "$integerPart" else "$integerPart.$fracStr"
    return formatBalanceWithSpaces("$sign$number")
}

fun formatBalanceWithSpaces(input: String): String {
    if (input.isEmpty() || input == "-") return input

    return try {
        val isNegative = input.startsWith("-")
        val cleanInput = if (isNegative) input.substring(1) else input

        if (cleanInput.isEmpty()) return input

        val parts = cleanInput.split(".")
        var integerPart = parts[0].filter { it.isDigit() }
        val decimalPart = if (parts.size > 1) ".${parts[1].take(2)}" else ""

        // Handle leading zeros
        integerPart = integerPart.trimStart('0')
        if (integerPart.isEmpty()) integerPart = "0"

        // Format with spaces
        val formattedInteger = buildString {
            for (i in integerPart.indices.reversed()) {
                val posFromEnd = integerPart.length - 1 - i
                if (posFromEnd > 0 && posFromEnd % 3 == 0) {
                    append(' ')
                }
                append(integerPart[i])
            }
        }.reversed()

        val result = "$formattedInteger$decimalPart"
        if (isNegative && result != "0") "-$result" else result
    } catch (e: Exception) {
        input
    }
}