package com.rango.delivery.core.format

import kotlin.math.roundToLong

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
