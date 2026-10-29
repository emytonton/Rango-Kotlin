package com.rango.delivery.feature.payment

import androidx.compose.ui.graphics.vector.ImageVector
import com.rango.delivery.core.designsystem.icon.RangoIcons

val PaymentType.icon: ImageVector
    get() = when (this) {
        PaymentType.Pix -> RangoIcons.QrCode2
        PaymentType.Credit -> RangoIcons.CreditCard
        PaymentType.Debit -> RangoIcons.CreditScore
        PaymentType.Cash -> RangoIcons.Payments
    }

fun paymentTypeOf(value: String): PaymentType? = PaymentType.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
