package com.rango.delivery.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoPlainField
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.feature.payment.CardField
import com.rango.delivery.feature.payment.CardForm
import com.rango.delivery.feature.payment.CardNumberTransformation
import com.rango.delivery.feature.payment.ExpiryCvvTransformation
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.payment_card_expiry_cvv
import com.rango.delivery.resources.payment_card_holder
import com.rango.delivery.resources.payment_card_number
import com.rango.delivery.resources.payment_card_secure
import com.rango.delivery.resources.payment_card_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CardFormCard(
    card: CardForm,
    onFieldChange: (CardField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.dp, colors.border, shape)
            .padding(15.dp),
    ) {
        Text(
            text = stringResource(Res.string.payment_card_title),
            style = Rango.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
            color = colors.text,
        )
        RangoPlainField(
            value = card.number,
            onValueChange = { onFieldChange(CardField.Number, it) },
            placeholder = stringResource(Res.string.payment_card_number),
            error = card.errors[CardField.Number],
            leadingIcon = RangoIcons.CreditCard,
            keyboardType = KeyboardType.Number,
            visualTransformation = CardNumberTransformation,
        )
        RangoPlainField(
            value = card.holder,
            onValueChange = { onFieldChange(CardField.Holder, it) },
            placeholder = stringResource(Res.string.payment_card_holder),
            error = card.errors[CardField.Holder],
            leadingIcon = RangoIcons.Person,
        )
        RangoPlainField(
            value = card.expiryCvv,
            onValueChange = { onFieldChange(CardField.ExpiryCvv, it) },
            placeholder = stringResource(Res.string.payment_card_expiry_cvv),
            error = card.errors[CardField.ExpiryCvv],
            leadingIcon = RangoIcons.CalendarMonth,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            visualTransformation = ExpiryCvvTransformation,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(RangoIcons.Lock, contentDescription = null, tint = colors.success, modifier = Modifier.size(17.dp))
            Text(
                text = stringResource(Res.string.payment_card_secure),
                style = Rango.typography.mono.copy(fontSize = 10.sp),
                color = colors.text2,
            )
        }
    }
}
