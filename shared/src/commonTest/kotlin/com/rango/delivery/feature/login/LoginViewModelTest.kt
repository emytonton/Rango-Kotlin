package com.rango.delivery.feature.login

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.auth.AuthRepository
import com.rango.delivery.data.auth.SignupData
import com.rango.delivery.data.auth.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyFieldsShowPrototypeMessages() = runTest {
        val viewModel = LoginViewModel(FakeAuthRepository(), successDelayMillis = 0)

        viewModel.onSubmit()

        assertEquals("Informe seu e-mail.", viewModel.uiState.value.emailError)
        assertEquals("Informe sua senha.", viewModel.uiState.value.passwordError)
    }

    @Test
    fun invalidEmailIsRejectedBeforeCallingBackend() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository, successDelayMillis = 0)
        viewModel.onEmailChange("marina@email")
        viewModel.onPasswordChange("rango123")

        viewModel.onSubmit()

        assertEquals("E-mail inválido.", viewModel.uiState.value.emailError)
        assertNull(repository.lastEmail)
    }

    @Test
    fun editingFieldClearsItsError() = runTest {
        val viewModel = LoginViewModel(FakeAuthRepository(), successDelayMillis = 0)
        viewModel.onSubmit()

        viewModel.onEmailChange("m")

        assertNull(viewModel.uiState.value.emailError)
        assertEquals("Informe sua senha.", viewModel.uiState.value.passwordError)
    }

    @Test
    fun validLoginSendsKeepSignedInAndNavigates() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository, successDelayMillis = 0)
        viewModel.onEmailChange("marina.alves@email.com")
        viewModel.onPasswordChange("rango123")
        viewModel.onToggleKeepSignedIn()

        viewModel.onSubmit()

        assertEquals(LoginEvent.LoggedIn, viewModel.events.first())
        assertEquals(false, repository.lastKeepSignedIn)
        assertTrue(viewModel.uiState.value.success)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun invalidCredentialsShowPasswordError() = runTest {
        val repository = FakeAuthRepository(
            failure = ApiException(401, "INVALID_CREDENTIALS", "E-mail ou senha incorretos."),
        )
        val viewModel = LoginViewModel(repository, successDelayMillis = 0)
        viewModel.onEmailChange("marina.alves@email.com")
        viewModel.onPasswordChange("errada")

        viewModel.onSubmit()

        assertEquals("Senha incorreta. Tente novamente.", viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.success)
    }

    private class FakeAuthRepository(private val failure: Exception? = null) : AuthRepository {
        var lastEmail: String? = null
        var lastKeepSignedIn: Boolean? = null

        override suspend fun login(email: String, password: String, keepSignedIn: Boolean): UserDto {
            failure?.let { throw it }
            lastEmail = email
            lastKeepSignedIn = keepSignedIn
            return UserDto(id = 1, name = "Marina Alves", email = email)
        }

        override suspend fun register(data: SignupData): UserDto = error("não usado no login")
    }
}
