package dc.stashguard.feature.operations.category_stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Slices smaller than this have no percentage on the ring; their row below still shows it. */
private const val MIN_LABELED_FRACTION = 0.05f

/** Space between neighboring slices, so same-colored ones stay apart. */
private const val SLICE_GAP_DEGREES = 1.5f

/**
 * A ring split into [slices] clockwise from the top, with [center] (the total) in the middle. With
 * [showPercentages] each slice is labeled with its share; a small chart leaves the labels out.
 * With no slices it is an empty grey ring. The slices sweep in whenever they change.
 */
@Composable
fun DonutChart(
    slices: List<CategorySlice>,
    colorOf: (CategorySlice) -> Color,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    strokeWidth: Dp = 40.dp,
    showPercentages: Boolean = true,
    center: @Composable () -> Unit = {}
) {
    val progress = remember(slices) { Animatable(0f) }
    LaunchedEffect(slices) { progress.animateTo(1f, tween(durationMillis = 600)) }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    val emptyColor = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = this.size.minDimension - stroke
            val topLeft = Offset((this.size.width - diameter) / 2, (this.size.height - diameter) / 2)
            // A single slice is a full ring, with no gap to leave
            val gap = if (slices.size > 1) SLICE_GAP_DEGREES else 0f
            if (slices.isEmpty()) {
                drawCircle(color = emptyColor, radius = diameter / 2, style = Stroke(width = stroke))
            }

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = 360f * slice.fraction * progress.value
                drawArc(
                    color = colorOf(slice),
                    startAngle = startAngle + gap / 2,
                    sweepAngle = (sweep - gap).coerceAtLeast(0f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )

                if (showPercentages && slice.fraction >= MIN_LABELED_FRACTION) {
                    // In the middle of the slice, on the ring's center line
                    val angle = (startAngle + sweep / 2) * PI.toFloat() / 180f
                    val radius = diameter / 2
                    val label = textMeasurer.measure(slice.fraction.percentText(), labelStyle)
                    drawText(
                        textLayoutResult = label,
                        topLeft = Offset(
                            x = this.center.x + radius * cos(angle) - label.size.width / 2,
                            y = this.center.y + radius * sin(angle) - label.size.height / 2
                        ),
                        alpha = progress.value
                    )
                }
                startAngle += sweep
            }
        }
        center()
    }
}
