package com.rango.delivery.core.designsystem.modifier

import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.ui.Modifier

actual fun Modifier.excludeSystemGestures(): Modifier = systemGestureExclusion()
