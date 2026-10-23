package com.rango.delivery.core.format

import kotlin.test.Test
import kotlin.test.assertEquals

class FormattersTest {

    @Test
    fun priceUsesBrazilianFormat() {
        assertEquals("R$ 34,90", formatPrice(34.9))
        assertEquals("R$ 45,00", formatPrice(45.0))
        assertEquals("R$ 9,90", formatPrice(9.9))
    }

    @Test
    fun ratingUsesCommaWithOneDecimal() {
        assertEquals("4,8", formatRating(4.8))
        assertEquals("5,0", formatRating(5.0))
    }

    @Test
    fun deliveryTimeShowsRange() {
        assertEquals("25-35 min", formatDeliveryTime(25, 35))
    }
}
