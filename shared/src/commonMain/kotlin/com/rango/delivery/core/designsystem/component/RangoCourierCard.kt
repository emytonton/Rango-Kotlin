package com.rango.delivery.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango

@Composable
fun RangoCourierCard(
    name: String,
    subtitle: String,
    photoUrl: String?,
    chatDescription: String,
    callDescription: String,
    onChat: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(15.dp),
    ) {
        RangoProductImage(
            imageUrl = photoUrl,
            hue = 250f,
            cornerRadius = 22.dp,
            modifier = Modifier.size(44.dp).clip(CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                color = colors.text,
            )
            Text(
                text = subtitle,
                style = Rango.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold),
                color = colors.text2,
            )
        }
        ContactButton(
            icon = RangoIcons.ChatBubbleOutline,
            description = chatDescription,
            onClick = onChat,
            background = colors.surface,
            tint = colors.primary,
            bordered = true,
        )
        ContactButton(
            icon = RangoIcons.Call,
            description = callDescription,
            onClick = onCall,
            background = colors.primary,
            tint = colors.onPrimary,
            bordered = false,
        )
    }
}

@Composable
private fun ContactButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    background: Color,
    tint: Color,
    bordered: Boolean,
) {
    val shape = RoundedCornerShape(13.dp)
    val borderColor = if (bordered) Rango.colors.border else Color.Transparent
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}
