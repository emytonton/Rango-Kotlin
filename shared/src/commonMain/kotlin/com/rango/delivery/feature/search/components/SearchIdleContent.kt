package com.rango.delivery.feature.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.feature.search.SearchSuggestion
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.search_recent_clear
import com.rango.delivery.resources.search_recent_empty
import com.rango.delivery.resources.search_recent_title
import com.rango.delivery.resources.search_suggestions_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchIdleContent(
    recent: List<String>,
    suggestions: List<SearchSuggestion>,
    onPickTerm: (String) -> Unit,
    onClearRecent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val titleStyle = Rango.typography.body.copy(fontWeight = FontWeight.ExtraBold)
    Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.search_recent_title), style = titleStyle, color = colors.text, modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.search_recent_clear),
                    style = Rango.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    modifier = Modifier.clickable(onClick = onClearRecent),
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recent.forEach { term -> RecentChip(term = term, onClick = { onPickTerm(term) }) }
                if (recent.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.search_recent_empty),
                        style = Rango.typography.body.copy(fontSize = 12.5.sp),
                        color = colors.text2,
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(stringResource(Res.string.search_suggestions_title), style = titleStyle, color = colors.text)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                suggestions.forEach { suggestion ->
                    SuggestionRow(suggestion = suggestion, onClick = { onPickTerm(suggestion.name) })
                }
            }
        }
    }
}

@Composable
private fun RecentChip(term: String, onClick: () -> Unit) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Icon(RangoIcons.History, contentDescription = null, tint = colors.text2, modifier = Modifier.size(16.dp))
        Text(text = term, style = Rango.typography.label.copy(fontSize = 12.5.sp), color = colors.text)
    }
}

@Composable
private fun SuggestionRow(suggestion: SearchSuggestion, onClick: () -> Unit) {
    val colors = Rango.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).background(colors.soft, RoundedCornerShape(12.dp)),
        ) {
            Icon(RangoIcons.TrendingUp, contentDescription = null, tint = colors.primary, modifier = Modifier.size(19.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.name,
                style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            Text(text = suggestion.hint, style = Rango.typography.caption, color = colors.text2)
        }
        Icon(RangoIcons.NorthEast, contentDescription = null, tint = colors.text2, modifier = Modifier.size(18.dp))
    }
}
