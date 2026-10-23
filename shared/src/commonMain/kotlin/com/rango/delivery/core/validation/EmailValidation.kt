package com.rango.delivery.core.validation

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[a-z]{2,}$", RegexOption.IGNORE_CASE)

fun isValidEmail(value: String): Boolean = emailRegex.matches(value.trim())
