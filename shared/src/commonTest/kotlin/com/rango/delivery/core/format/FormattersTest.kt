package com.rango.delivery.core.format

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

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

    @Test
    fun orderDateShowsTodayYesterdayOrDayAndMonth() {
        val now = Instant.parse("2026-11-05T22:00:00Z")
        assertEquals("Hoje · 19:42", formatOrderDate("2026-11-05T19:42:00.000Z", now, TimeZone.UTC))
        assertEquals("Ontem · 08:05", formatOrderDate("2026-11-04T08:05:00.000Z", now, TimeZone.UTC))
        assertEquals("12 ago · 20:15", formatOrderDate("2026-08-12T20:15:00.000Z", now, TimeZone.UTC))
    }

    @Test
    fun orderDateUsesTheGivenTimeZone() {
        val now = Instant.parse("2026-11-05T12:00:00Z")
        val saoPaulo = TimeZone.of("America/Sao_Paulo")
        assertEquals("Ontem · 22:30", formatOrderDate("2026-11-05T01:30:00.000Z", now, saoPaulo))
    }
}
