package com.rango.delivery.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ToastKind { Success, Warning, Error }

data class ToastMessage(val text: String, val kind: ToastKind = ToastKind.Success)

@Stable
class ToastState internal constructor(private val scope: CoroutineScope) {
    var current by mutableStateOf<ToastMessage?>(null)
        private set

    private var dismissJob: Job? = null

    fun show(text: String, kind: ToastKind = ToastKind.Success) {
        dismissJob?.cancel()
        current = ToastMessage(text, kind)
        dismissJob = scope.launch {
            delay(TOAST_DURATION_MS)
            current = null
        }
    }

    private companion object {
        const val TOAST_DURATION_MS = 2_600L
    }
}

val LocalToast = staticCompositionLocalOf<ToastState> { error("ToastState não fornecido") }

@Composable
fun rememberToastState(): ToastState {
    val scope = rememberCoroutineScope()
    return remember(scope) { ToastState(scope) }
}

@Composable
fun BoxScope.RangoToastHost(state: ToastState) {
    var last by remember { mutableStateOf<ToastMessage?>(null) }
    state.current?.let { last = it }
    AnimatedVisibility(
        visible = state.current != null,
        enter = slideInVertically { it / 2 } + fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp, end = 16.dp, bottom = 30.dp),
    ) {
        last?.let { RangoToast(it) }
    }
}

@Composable
fun RangoToast(message: ToastMessage, modifier: Modifier = Modifier) {
    val colors = Rango.colors
    val accent = when (message.kind) {
        ToastKind.Success -> Color(0xFF31C25C)
        ToastKind.Warning -> Color(0xFFFF8A34)
        ToastKind.Error -> Color(0xFFFF5F58)
    }
    val icon = when (message.kind) {
        ToastKind.Success -> RangoIcons.CheckCircle
        ToastKind.Warning -> RangoIcons.Info
        ToastKind.Error -> RangoIcons.Error
    }
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(if (colors.isDark) Color(0xFF2C2E33) else Color(0xFF26262A)),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accent),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(
                text = message.text,
                style = Rango.typography.label.copy(fontSize = 13.sp, lineHeight = 17.sp),
                color = Color.White,
            )
        }
    }
}
