package com.rango.delivery.feature.signup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignupValidatorTest {

    private val validForm = SignupForm(
        name = "Marina Alves",
        email = "marina.alves@email.com",
        phone = "(11) 98876-4410",
        password = "rango123",
        confirmation = "rango123",
    )

    @Test
    fun validFormHasNoErrors() {
        assertTrue(SignupValidator.validate(validForm).isEmpty())
    }

    @Test
    fun emptyFormShowsPrototypeMessages() {
        val errors = SignupValidator.validate(SignupForm())

        assertEquals("Informe seu nome completo.", errors[SignupField.Name])
        assertEquals("E-mail inválido.", errors[SignupField.Email])
        assertEquals("Telefone incompleto.", errors[SignupField.Phone])
        assertEquals("Mínimo de 6 caracteres.", errors[SignupField.Password])
        assertEquals("As senhas não coincidem.", errors[SignupField.Confirmation])
    }

    @Test
    fun nameNeedsThreeCharactersIgnoringSpaces() {
        val errors = SignupValidator.validate(validForm.copy(name = "  Ma  "))

        assertEquals(setOf(SignupField.Name), errors.keys)
    }

    @Test
    fun phoneCountsOnlyDigits() {
        assertTrue(SignupField.Phone in SignupValidator.validate(validForm.copy(phone = "(11) 9887-")))
        assertTrue(SignupField.Phone !in SignupValidator.validate(validForm.copy(phone = "1198876441")))
    }

    @Test
    fun confirmationMustMatchPassword() {
        val errors = SignupValidator.validate(validForm.copy(confirmation = "rango124"))

        assertEquals(setOf(SignupField.Confirmation), errors.keys)
    }
}
