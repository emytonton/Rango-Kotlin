package com.rango.delivery.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.component.RangoSkeleton

@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.padding(horizontal = 20.dp)) {
        RangoSkeleton(Modifier.fillMaxWidth().height(118.dp), cornerRadius = 20.dp)
        RangoSkeleton(Modifier.width(150.dp).height(16.dp), shimmer = false)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.width(176.dp)) {
                    RangoSkeleton(Modifier.fillMaxWidth().height(112.dp), cornerRadius = 16.dp)
                    RangoSkeleton(Modifier.fillMaxWidth(0.8f).height(12.dp), shimmer = false)
                    RangoSkeleton(Modifier.fillMaxWidth(0.55f).height(10.dp), shimmer = false)
                }
            }
        }
        RangoSkeleton(Modifier.width(120.dp).height(16.dp), shimmer = false)
        repeat(2) { ProductRowSkeleton() }
    }
}

@Composable
fun ProductRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        RangoSkeleton(Modifier.size(84.dp), cornerRadius = 16.dp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            RangoSkeleton(Modifier.fillMaxWidth(0.7f).height(13.dp), shimmer = false)
            RangoSkeleton(Modifier.fillMaxWidth(0.9f).height(10.dp), shimmer = false)
            RangoSkeleton(Modifier.fillMaxWidth(0.4f).height(10.dp), shimmer = false)
        }
    }
}
