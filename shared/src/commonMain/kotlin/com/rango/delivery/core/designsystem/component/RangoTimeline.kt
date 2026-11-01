package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

enum class TimelineState { Done, Current, Upcoming }

data class TimelineEntry(
    val label: String,
    val time: String,
    val icon: ImageVector,
    val state: TimelineState,
)

private val DotSize = 26.dp

@Composable
fun RangoTimeline(entries: List<TimelineEntry>, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        entries.forEachIndexed { index, entry ->
            TimelineRow(entry = entry, showLine = index < entries.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(entry: TimelineEntry, showLine: Boolean) {
    val colors = Rango.colors
    Row(horizontalArrangement = Arrangement.spacedBy(13.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(DotSize)) {
            TimelineDot(entry)
            if (showLine) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .heightIn(min = 26.dp)
                        .background(if (entry.state == TimelineState.Done) colors.success else colors.border),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f).padding(bottom = 16.dp),
        ) {
            Text(
                text = entry.label,
                style = Rango.typography.body.copy(
                    fontSize = 14.sp,
                    fontWeight = if (entry.state == TimelineState.Current) FontWeight.ExtraBold else FontWeight.Bold,
                ),
                color = when (entry.state) {
                    TimelineState.Current -> colors.primary
                    TimelineState.Done -> colors.text
                    TimelineState.Upcoming -> colors.text2
                },
            )
            Text(
                text = entry.time,
                style = Rango.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text2,
            )
        }
    }
}

@Composable
private fun TimelineDot(entry: TimelineEntry) {
    val colors = Rango.colors
    when (entry.state) {
        TimelineState.Upcoming -> Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(DotSize)
                .background(colors.surface2, CircleShape)
                .border(1.5.dp, colors.border, CircleShape),
        ) {
            Icon(entry.icon, contentDescription = null, tint = colors.text2, modifier = Modifier.size(15.dp))
        }
        else -> {
            val done = entry.state == TimelineState.Done
            RangoPulseCircle(
                size = DotSize,
                color = if (done) colors.success else colors.primary,
                pulsing = !done,
            ) {
                Icon(
                    imageVector = if (done) RangoIcons.Check else entry.icon,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}
