package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoEditableAvatar(
    initials: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    contentDescription: String? = null,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(34.dp)
    Box(modifier = modifier.size(106.dp)) {
        Box(modifier = Modifier.matchParentSize().clip(shape).clickable(enabled = !loading, onClick = onClick)) {
            RangoAvatar(initials = initials, imageUrl = imageUrl, size = 106.dp, cornerRadius = 34.dp, fontSize = 30.sp)
            if (loading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)),
                ) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = !loading, onClick = onClick)
                .background(colors.primary, RoundedCornerShape(12.dp))
                .border(3.dp, colors.surface, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector = RangoIcons.PhotoCamera,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}
