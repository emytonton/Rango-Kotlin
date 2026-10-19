package com.rango.delivery.feature.signup

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.ApiFieldError
import com.rango.delivery.data.auth.AuthRepository
import com.rango.delivery.data.auth.SignupData
import com.rango.delivery.data.auth.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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
class SignupViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun errorsAppearOnlyAfterFirstSubmit() = runTest {
        val viewModel = SignupViewModel(FakeAuthRepository())

        assertTrue(viewModel.uiState.value.errors.isEmpty())
        viewModel.onSubmit()

        assertEquals(5, viewModel.uiState.value.errors.size)
    }

    @Test
    fun submitWithoutTermsShowsToastAndDoesNotRegister() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SignupViewModel(repository).fillValidForm()

        viewModel.onSubmit()

        assertEquals(
            SignupEvent.ShowToast("Aceite os termos para continuar.", ToastKind.Error),
            viewModel.events.first(),
        )
        assertNull(repository.received)
    }

    @Test
    fun validSubmitRegistersAndNavigates() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SignupViewModel(repository).fillValidForm()
        viewModel.onTermsToggle()

        viewModel.onSubmit()

        assertEquals(
            listOf(
                SignupEvent.ShowToast("Conta criada com sucesso!", ToastKind.Success),
                SignupEvent.Registered,
            ),
            viewModel.events.take(2).toList(),
        )
        assertEquals("marina.alves@email.com", repository.received?.email)
        assertFalse(viewModel.uiState.value.loading)
    }

    @Test
    fun emailInUseMarksEmailField() = runTest {
        val repository = FakeAuthRepository(
            failure = ApiException(
                status = 409,
                code = "EMAIL_IN_USE",
                message = "Este e-mail já está cadastrado.",
                details = listOf(ApiFieldError("email", "Este e-mail já está cadastrado.")),
            ),
        )
        val viewModel = SignupViewModel(repository).fillValidForm()
        viewModel.onTermsToggle()

        viewModel.onSubmit()

        assertEquals("Este e-mail já está cadastrado.", viewModel.uiState.value.errors[SignupField.Email])
        viewModel.onFieldChange(SignupField.Email, "outra@email.com")
        assertNull(viewModel.uiState.value.errors[SignupField.Email])
    }

    private fun SignupViewModel.fillValidForm(): SignupViewModel = apply {
        onFieldChange(SignupField.Name, "Marina Alves")
        onFieldChange(SignupField.Email, "marina.alves@email.com")
        onFieldChange(SignupField.Phone, "(11) 98876-4410")
        onFieldChange(SignupField.Password, "rango123")
        onFieldChange(SignupField.Confirmation, "rango123")
    }

    private class FakeAuthRepository(private val failure: Exception? = null) : AuthRepository {
        var received: SignupData? = null

        override suspend fun login(email: String, password: String, keepSignedIn: Boolean): UserDto =
            error("não usado no cadastro")

        override suspend fun register(data: SignupData): UserDto {
            failure?.let { throw it }
            received = data
            return UserDto(id = 1, name = data.name, email = data.email, phone = data.phone)
        }
    }
}
