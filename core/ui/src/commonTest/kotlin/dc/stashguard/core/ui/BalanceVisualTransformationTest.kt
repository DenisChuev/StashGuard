package dc.stashguard.core.ui

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BalanceVisualTransformationTest {

    private val transformation = BalanceVisualTransformation()

    @Test
    fun formatsEmptyString() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
    }

    @Test
    fun formatsSingleDigit() {
        val result = transformation.filter(AnnotatedString("5"))
        assertEquals("5", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(1, result.offsetMapping.originalToTransformed(1))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
        assertEquals(1, result.offsetMapping.transformedToOriginal(1))
    }

    @Test
    fun formatsFourDigitsWithSingleSpace() {
        // "1000" formatted as "1 000"
        val result = transformation.filter(AnnotatedString("1000"))
        assertEquals("1 000", result.text.text)

        val mapping = result.offsetMapping

        // originalToTransformed:
        // offset 0 (before '1') -> 0
        // offset 1 (after '1') -> 2 (after '1 ')
        // offset 2 (after '10') -> 3
        // offset 3 (after '100') -> 4
        // offset 4 (after '1000') -> 5
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(2, mapping.originalToTransformed(1))
        assertEquals(3, mapping.originalToTransformed(2))
        assertEquals(4, mapping.originalToTransformed(3))
        assertEquals(5, mapping.originalToTransformed(4))

        // transformedToOriginal:
        // offset 0 -> 0
        // offset 1 (before space) -> 0
        // offset 2 (after space) -> 1
        // offset 3 -> 2
        // offset 4 -> 3
        // offset 5 -> 4
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(0, mapping.transformedToOriginal(1))
        assertEquals(1, mapping.transformedToOriginal(2))
        assertEquals(2, mapping.transformedToOriginal(3))
        assertEquals(3, mapping.transformedToOriginal(4))
        assertEquals(4, mapping.transformedToOriginal(5))
    }

    @Test
    fun backspacingOverSpaceDeletesDigitBeforeSpaceInOneClick() {
        // Formatted: "10 000", raw: "10000"
        val result = transformation.filter(AnnotatedString("10000"))
        assertEquals("10 000", result.text.text)

        val mapping = result.offsetMapping

        // In "10 000", space is at visual index 2 (between offset 2 and 3: "10 | 000")
        // When cursor is at offset 3 ("10 |000") and user presses Backspace:
        // Compose deletes visual range (2, 3).
        val startOriginal = mapping.transformedToOriginal(2)
        val endOriginal = mapping.transformedToOriginal(3)

        // Visual range (2, 3) must map to original range (1, 2), deleting digit '0' at original index 1
        assertEquals(1, startOriginal)
        assertEquals(2, endOriginal)
    }

    @Test
    fun monotonicityOfOffsetMapping() {
        val testStrings = listOf("1", "12", "123", "1234", "12345", "1234567", "1000.50")
        for (str in testStrings) {
            val result = transformation.filter(AnnotatedString(str))
            val mapping = result.offsetMapping
            val formattedLength = result.text.text.length

            // Verify originalToTransformed monotonicity
            var prevTransformed = 0
            for (i in 0..str.length) {
                val transformed = mapping.originalToTransformed(i)
                assertTrue(
                    transformed >= prevTransformed,
                    "originalToTransformed($i) = $transformed should be >= $prevTransformed for string '$str'"
                )
                assertTrue(
                    transformed in 0..formattedLength,
                    "originalToTransformed($i) = $transformed out of bounds 0..$formattedLength"
                )
                prevTransformed = transformed
            }

            // Verify transformedToOriginal monotonicity
            var prevOriginal = 0
            for (i in 0..formattedLength) {
                val orig = mapping.transformedToOriginal(i)
                assertTrue(
                    orig >= prevOriginal,
                    "transformedToOriginal($i) = $orig should be >= $prevOriginal for string '$str'"
                )
                assertTrue(
                    orig in 0..str.length,
                    "transformedToOriginal($i) = $orig out of bounds 0..${str.length}"
                )
                prevOriginal = orig
            }
        }
    }
}
