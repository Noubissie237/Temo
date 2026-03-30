package com.propentatech.kumbaka.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
// CAMEMBERT (Pie Chart)
// ─────────────────────────────────────────────

data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    centerLabel: String = ""
) {
    if (slices.isEmpty()) return

    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    var triggered by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "pieAnim"
    )
    LaunchedEffect(Unit) { triggered = true }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweep = (slice.value / total) * 360f * animProgress
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(40f, 40f),
                        size = Size(size.width - 80f, size.height - 80f),
                        style = Stroke(width = 56f, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            if (centerLabel.isNotEmpty()) {
                Text(centerLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Légende
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEach { slice ->
                val pct = if (total > 0) (slice.value / total * 100).toInt() else 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(slice.color))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(slice.label, fontSize = 12.sp, modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// COURBE DE TENDANCE (Line Chart)
// ─────────────────────────────────────────────

@Composable
fun TrendLineChart(
    points: List<Float>,
    lineColor: Color = Color(0xFFFF6B00),
    modifier: Modifier = Modifier,
    label: String = "Tendance"
) {
    if (points.size < 2) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text("Pas assez de données", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxVal = points.max()
    val minVal = points.min()
    val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

    var triggered by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "lineAnim"
    )
    LaunchedEffect(Unit) { triggered = true }
    val drawCount = (points.size * animProgress).toInt().coerceAtLeast(2)

    Column(modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        .padding(16.dp)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(12.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            val w = size.width
            val h = size.height
            val step = w / (points.size - 1).toFloat()

            // Gradient fill path
            val fillPath = Path().apply {
                moveTo(0f, h)
                points.take(drawCount).forEachIndexed { i, v ->
                    val x = i * step
                    val y = h - ((v - minVal) / range) * h * 0.85f
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                lineTo((drawCount - 1) * step, h)
                close()
            }
            drawPath(fillPath, lineColor.copy(alpha = 0.15f))

            // Line
            val path = Path().apply {
                points.take(drawCount).forEachIndexed { i, v ->
                    val x = i * step
                    val y = h - ((v - minVal) / range) * h * 0.85f
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
            }
            drawPath(path, lineColor, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Last dot
            val lastX = (drawCount - 1) * step
            val lastY = h - ((points[drawCount - 1] - minVal) / range) * h * 0.85f
            drawCircle(lineColor, radius = 8f, center = Offset(lastX, lastY))
            drawCircle(Color.White, radius = 4f, center = Offset(lastX, lastY))
        }
    }
}
