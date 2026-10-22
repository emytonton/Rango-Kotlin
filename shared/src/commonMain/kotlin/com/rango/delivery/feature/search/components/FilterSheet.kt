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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoFilterChip
import com.rango.delivery.core.designsystem.component.RangoRadioRow
import com.rango.delivery.core.designsystem.component.RangoSlider
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.feature.search.SearchCategory
import com.rango.delivery.feature.search.SearchFilters
import com.rango.delivery.feature.search.SearchSort
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.search_all_categories
import com.rango.delivery.resources.search_apply_filters
import com.rango.delivery.resources.search_category
import com.rango.delivery.resources.search_clear_filters
import com.rango.delivery.resources.search_close_filters
import com.rango.delivery.resources.search_filters_title
import com.rango.delivery.resources.search_price
import com.rango.delivery.resources.search_price_max
import com.rango.delivery.resources.search_price_min
import com.rango.delivery.resources.search_price_up_to
import com.rango.delivery.resources.search_rating
import com.rango.delivery.resources.search_sort
import com.rango.delivery.resources.search_time
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    draft: SearchFilters,
    categories: List<SearchCategory>,
    onChange: (SearchFilters) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Rango.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        containerColor = colors.surface,
        scrimColor = Color.Black.copy(alpha = 0.45f),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 14.dp)
                    .size(width = 44.dp, height = 4.5.dp)
                    .background(colors.border, RoundedCornerShape(3.dp)),
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
        ) {
            SheetHeader(onClose = onDismiss)
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            ) {
                CategorySection(draft, categories, onChange)
                PriceSection(draft, onChange)
                OptionSection(
                    title = stringResource(Res.string.search_rating),
                    options = SearchFilters.ratingOptions,
                    selected = draft.minRating,
                    onSelect = { onChange(draft.copy(minRating = it)) },
                )
                OptionSection(
                    title = stringResource(Res.string.search_time),
                    options = SearchFilters.timeOptions,
                    selected = draft.maxTime,
                    onSelect = { onChange(draft.copy(maxTime = it)) },
                )
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    SectionTitle(stringResource(Res.string.search_sort))
                    SearchSort.entries.forEach { sort ->
                        RangoRadioRow(label = sort.optionLabel, selected = draft.sort == sort, onClick = { onChange(draft.copy(sort = sort)) })
                    }
                }
            }
            SheetActions(onClear = onClear, onApply = onApply)
        }
    }
}

@Composable
private fun SheetHeader(onClose: () -> Unit) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(12.dp)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.search_filters_title),
            style = Rango.typography.section.copy(fontSize = 18.sp, letterSpacing = (-0.5).sp),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(shape)
                .background(colors.surface)
                .border(1.dp, colors.border, shape)
                .clickable(onClick = onClose),
        ) {
            Icon(RangoIcons.Close, contentDescription = stringResource(Res.string.search_close_filters), tint = colors.text, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = Rango.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold), color = Rango.colors.text)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(draft: SearchFilters, categories: List<SearchCategory>, onChange: (SearchFilters) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionTitle(stringResource(Res.string.search_category))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            RangoFilterChip(
                label = stringResource(Res.string.search_all_categories),
                selected = draft.category == null,
                onClick = { onChange(draft.copy(category = null)) },
            )
            categories.forEach { category ->
                RangoFilterChip(
                    label = category.name,
                    selected = draft.category?.slug == category.slug,
                    onClick = { onChange(draft.copy(category = category)) },
                )
            }
        }
    }
}

@Composable
private fun PriceSection(draft: SearchFilters, onChange: (SearchFilters) -> Unit) {
    val colors = Rango.colors
    val smallLabel = Rango.typography.label.copy(fontSize = 11.sp)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { SectionTitle(stringResource(Res.string.search_price)) }
            Text(
                text = stringResource(Res.string.search_price_up_to, formatPrice(draft.maxPrice.toDouble())),
                style = Rango.typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold),
                color = colors.primary,
            )
        }
        RangoSlider(
            value = draft.maxPrice.toFloat(),
            onValueChange = { onChange(draft.copy(maxPrice = it.roundToInt())) },
            valueRange = SearchFilters.PRICE_MIN.toFloat()..SearchFilters.PRICE_MAX.toFloat(),
            steps = (SearchFilters.PRICE_MAX - SearchFilters.PRICE_MIN) / SearchFilters.PRICE_STEP - 1,
            modifier = Modifier.fillMaxWidth().height(28.dp),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.search_price_min), style = smallLabel, color = colors.text2, modifier = Modifier.weight(1f))
            Text(stringResource(Res.string.search_price_max), style = smallLabel, color = colors.text2)
        }
    }
}

@Composable
private fun <T> OptionSection(title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        SectionTitle(title)
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            options.forEach { (value, label) ->
                RangoFilterChip(label = label, selected = value == selected, onClick = { onSelect(value) })
            }
        }
    }
}

@Composable
private fun SheetActions(onClear: () -> Unit, onApply: () -> Unit) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(16.dp)
    val textStyle = Rango.typography.body.copy(fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(shape)
                .border(1.5.dp, colors.border, shape)
                .clickable(onClick = onClear),
        ) {
            Text(stringResource(Res.string.search_clear_filters), style = textStyle, color = colors.text)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1.4f)
                .height(52.dp)
                .clip(shape)
                .background(colors.primary)
                .clickable(onClick = onApply),
        ) {
            Text(stringResource(Res.string.search_apply_filters), style = textStyle, color = colors.onPrimary)
        }
    }
}
