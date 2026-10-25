package com.rango.delivery.feature.product.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.component.RangoSkeleton

@Composable
fun ProductSkeleton(modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.fillMaxWidth()) {
        RangoSkeleton(Modifier.fillMaxWidth().height(296.dp), cornerRadius = 0.dp)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
            RangoSkeleton(Modifier.fillMaxWidth(0.7f).height(22.dp))
            RangoSkeleton(Modifier.fillMaxWidth(0.45f).height(13.dp), shimmer = false)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { RangoSkeleton(Modifier.weight(1f).height(72.dp), cornerRadius = 15.dp, shimmer = false) }
            }
            RangoSkeleton(Modifier.fillMaxWidth().height(12.dp), shimmer = false)
            RangoSkeleton(Modifier.fillMaxWidth(0.8f).height(12.dp), shimmer = false)
        }
    }
}
