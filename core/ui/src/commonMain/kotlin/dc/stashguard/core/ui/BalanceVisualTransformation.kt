package dc.stashguard.core.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import dc.stashguard.core.common.formatBalanceWithSpaces

/** Shows a raw amount like "200300.5" grouped as "200 300.5" while the field keeps the raw text. */
class BalanceVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val formatted = formatBalanceWithSpaces(original)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= original.length) return formatted.length

                val nonSpaceCountInOriginal = original.take(offset).count { it != ' ' }
                var currentNonSpaceCount = 0
                for (i in formatted.indices) {
                    if (formatted[i] != ' ') {
                        currentNonSpaceCount++
                    }
                    if (currentNonSpaceCount >= nonSpaceCountInOriginal) {
                        return i + 1
                    }
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= formatted.length) return original.length

                val nonSpaceCountInFormatted = formatted.take(offset).count { it != ' ' }
                return nonSpaceCountInFormatted.coerceAtMost(original.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
