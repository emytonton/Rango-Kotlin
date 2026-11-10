package com.rango.delivery.core.format

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToLong
import kotlin.time.Instant

private val MONTHS = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

fun formatPrice(value: Double): String {
    val cents = (value * 100).roundToLong()
    val reais = cents / 100
    val centavos = (cents % 100).toString().padStart(2, '0')
    return "R$ $reais,$centavos"
}

fun formatRating(value: Double): String {
    val tenths = (value * 10).roundToLong()
    return "${tenths / 10},${tenths % 10}"
}

fun formatDeliveryTime(min: Int, max: Int): String = "$min-$max min"

fun formatOrderDate(iso: String, now: Instant, timeZone: TimeZone): String {
    val local = Instant.parse(iso).toLocalDateTime(timeZone)
    val today = now.toLocalDateTime(timeZone).date
    val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    val day = when (local.date) {
        today -> "Hoje"
        today.minus(DatePeriod(days = 1)) -> "Ontem"
        else -> "${local.day.toString().padStart(2, '0')} ${MONTHS[local.month.ordinal]}"
    }
    return "$day · $time"
}

fun formatRelativeTime(iso: String, now: Instant, timeZone: TimeZone): String {
    val instant = Instant.parse(iso)
    val minutes = (now - instant).inWholeMinutes
    if (minutes < 1) return "agora"
    if (minutes < 60) return "há $minutes min"
    val local = instant.toLocalDateTime(timeZone).date
    val today = now.toLocalDateTime(timeZone).date
    return when {
        local == today -> "há ${minutes / 60} h"
        local == today.minus(DatePeriod(days = 1)) -> "ontem"
        else -> "${local.day.toString().padStart(2, '0')} ${MONTHS[local.month.ordinal]}"
    }
}
