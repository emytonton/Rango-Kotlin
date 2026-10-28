package com.rango.delivery.feature.cart

import com.rango.delivery.core.designsystem.component.SummaryLine
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.data.cart.CartDto

fun CartDto.summaryLines(): List<SummaryLine> = buildList {
    add(SummaryLine("Subtotal", formatPrice(summary.subtotal)))
    val free = summary.deliveryFee == 0.0 && summary.subtotal > 0
    add(SummaryLine("Taxa de entrega", if (free) "Grátis" else formatPrice(summary.deliveryFee), highlight = free))
    if (summary.discount > 0) {
        val code = coupon?.takeIf { it.applied }?.code.orEmpty()
        add(SummaryLine("Desconto · $code", "- ${formatPrice(summary.discount)}", highlight = true))
    }
}
