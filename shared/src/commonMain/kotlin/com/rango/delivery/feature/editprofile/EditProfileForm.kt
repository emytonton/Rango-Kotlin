package com.rango.delivery.feature.editprofile

import com.rango.delivery.core.validation.isValidEmail

enum class ProfileField(val apiName: String) {
    Name("name"),
    Email("email"),
    Phone("phone"),
}

data class EditProfileForm(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
) {
    fun valueOf(field: ProfileField): String = when (field) {
        ProfileField.Name -> name
        ProfileField.Email -> email
        ProfileField.Phone -> phone
    }

    fun with(field: ProfileField, value: String): EditProfileForm = when (field) {
        ProfileField.Name -> copy(name = value)
        ProfileField.Email -> copy(email = value)
        ProfileField.Phone -> copy(phone = value)
    }

    fun validate(): Map<ProfileField, String> = buildMap {
        if (name.trim().length < 3) put(ProfileField.Name, "Informe seu nome completo.")
        if (!isValidEmail(email)) put(ProfileField.Email, "E-mail inválido.")
        if (phone.count(Char::isDigit) !in 10..11) put(ProfileField.Phone, "Telefone incompleto.")
    }
}
