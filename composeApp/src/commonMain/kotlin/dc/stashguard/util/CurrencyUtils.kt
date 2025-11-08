package dc.stashguard.util

import kotlin.math.pow

fun Double.toBalanceString(maxFractionDigits: Int = 2): String {
    if (this == 0.0) return "0"

    val integerPart = toLong()
    val remainder = kotlin.math.abs(this - integerPart)

    if (remainder == 0.0) {
        return integerPart.toString()
    }

    // Round to maxFractionDigits to avoid floating garbage
    val multiplier = 10.0.pow(maxFractionDigits)
    val roundedRemainder = kotlin.math.round(remainder * multiplier)
    val fracStr =
        roundedRemainder.toInt().toString().padStart(maxFractionDigits, '0').removeSuffix("0")

    return if (fracStr.isEmpty()) {
        integerPart.toString()
    } else {
        formatBalanceWithSpaces("$integerPart.$fracStr")
    }
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