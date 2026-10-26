package com.rango.delivery.feature.address.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rango.delivery.core.designsystem.component.RangoPlainField
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.feature.address.AddressField
import com.rango.delivery.feature.address.AddressForm
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.address_cep
import com.rango.delivery.resources.address_complement
import com.rango.delivery.resources.address_district
import com.rango.delivery.resources.address_new_title
import com.rango.delivery.resources.address_save
import com.rango.delivery.resources.address_street
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddressFormCard(
    form: AddressForm,
    saving: Boolean,
    onFieldChange: (AddressField, String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Rango.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(11.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface, shape)
            .border(1.5.dp, colors.primary, shape)
            .padding(15.dp),
    ) {
        Text(
            text = stringResource(Res.string.address_new_title),
            style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold),
            color = colors.text,
        )
        RangoPlainField(
            value = form.cep,
            onValueChange = { onFieldChange(AddressField.Cep, it) },
            placeholder = stringResource(Res.string.address_cep),
            error = form.errors[AddressField.Cep],
            keyboardType = KeyboardType.Number,
        )
        RangoPlainField(
            value = form.street,
            onValueChange = { onFieldChange(AddressField.Street, it) },
            placeholder = stringResource(Res.string.address_street),
            error = form.errors[AddressField.Street],
        )
        RangoPlainField(
            value = form.complement,
            onValueChange = { onFieldChange(AddressField.Complement, it) },
            placeholder = stringResource(Res.string.address_complement),
            error = form.errors[AddressField.Complement],
        )
        RangoPlainField(
            value = form.district,
            onValueChange = { onFieldChange(AddressField.District, it) },
            placeholder = stringResource(Res.string.address_district),
            error = form.errors[AddressField.District],
            imeAction = ImeAction.Done,
        )
        RangoPrimaryButton(
            text = stringResource(Res.string.address_save),
            onClick = onSave,
            loading = saving,
            modifier = Modifier.height(48.dp),
        )
    }
}
