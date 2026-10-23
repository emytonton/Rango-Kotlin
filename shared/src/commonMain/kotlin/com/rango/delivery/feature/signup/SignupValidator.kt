package com.rango.delivery.feature.signup

enum class SignupField(val apiName: String) {
    Name("name"),
    Email("email"),
    Phone("phone"),
    Password("password"),
    Confirmation("passwordConfirmation"),
}

data class SignupForm(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmation: String = "",
) {
    fun valueOf(field: SignupField): String = when (field) {
        SignupField.Name -> name
        SignupField.Email -> email
        SignupField.Phone -> phone
        SignupField.Password -> password
        SignupField.Confirmation -> confirmation
    }

    fun with(field: SignupField, value: String): SignupForm = when (field) {
        SignupField.Name -> copy(name = value)
        SignupField.Email -> copy(email = value)
        SignupField.Phone -> copy(phone = value)
        SignupField.Password -> copy(password = value)
        SignupField.Confirmation -> copy(confirmation = value)
    }
}

object SignupValidator {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[a-z]{2,}$", RegexOption.IGNORE_CASE)

    fun validate(form: SignupForm): Map<SignupField, String> = buildMap {
        if (form.name.trim().length < 3) put(SignupField.Name, "Informe seu nome completo.")
        if (!emailRegex.matches(form.email.trim())) put(SignupField.Email, "E-mail inválido.")
        if (form.phone.count(Char::isDigit) < 10) put(SignupField.Phone, "Telefone incompleto.")
        if (form.password.length < 6) put(SignupField.Password, "Mínimo de 6 caracteres.")
        if (form.confirmation.isEmpty() || form.confirmation != form.password) {
            put(SignupField.Confirmation, "As senhas não coincidem.")
        }
    }
}
