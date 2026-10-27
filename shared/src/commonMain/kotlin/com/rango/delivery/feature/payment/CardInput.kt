package com.rango.delivery.feature.payment

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

const val CARD_NUMBER_MAX = 19
const val EXPIRY_CVV_MAX = 8

object CardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val grouped = digits.chunked(4).joinToString(" ")
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset == 0) 0 else (offset + (offset - 1) / 4).coerceAtMost(grouped.length)

            override fun transformedToOriginal(offset: Int): Int = (offset - offset / 5).coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

object ExpiryCvvTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            digits.forEachIndexed { index, char ->
                if (index == 2) append('/')
                if (index == 4) append(' ')
                append(char)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 4 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(formatted.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                else -> offset - 2
            }.coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

object CardRules {

    private val eloPrefixes = listOf("4011", "4312", "4389", "4514", "4576", "5041", "5066", "5067", "509", "6277", "6362", "6363", "650", "6516", "6550")

    fun brandOf(number: String): String {
        val two = number.take(2).toIntOrNull() ?: 0
        val four = number.take(4).toIntOrNull() ?: 0
        return when {
            eloPrefixes.any { number.startsWith(it) } -> "Elo"
            number.startsWith("4") -> "Visa"
            two in 51..55 || four in 2221..2720 -> "Mastercard"
            two == 34 || two == 37 -> "Amex"
            else -> "Cartão"
        }
    }

    fun isValidNumber(number: String): Boolean {
        if (number.length !in 13..CARD_NUMBER_MAX) return false
        val sum = number.reversed().mapIndexed { index, char ->
            val digit = char.digitToInt()
            if (index % 2 == 1) (digit * 2).let { if (it > 9) it - 9 else it } else digit
        }.sum()
        return sum % 10 == 0
    }

    fun isValidExpiryCvv(value: String): Boolean {
        if (value.length !in 7..EXPIRY_CVV_MAX) return false
        val month = value.take(2).toInt()
        return month in 1..12
    }
}
