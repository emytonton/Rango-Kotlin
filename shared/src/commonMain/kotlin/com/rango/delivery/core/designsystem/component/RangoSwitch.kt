package com.rango.delivery.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.rango.delivery.core.designsystem.theme.Rango

private val TrackShape = RoundedCornerShape(12.dp)
private const val ANIMATION_MS = 180

@Composable
fun RangoSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    stateDescription: String? = null,
) {
    val colors = Rango.colors
    val track by animateColorAsState(
        targetValue = if (checked) colors.primary else colors.border,
        animationSpec = tween(ANIMATION_MS),
    )
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 17.dp else 0.dp,
        animationSpec = tween(ANIMATION_MS),
    )
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(width = 40.dp, height = 23.dp)
            .background(track, TrackShape)
            .padding(2.5.dp)
            .semantics {
                role = Role.Switch
                if (stateDescription != null) this.stateDescription = stateDescription
            },
    ) {
        Box(
            Modifier
                .offset(x = knobOffset)
                .size(18.dp)
                .background(Color.White, CircleShape),
        )
    }
}
