package com.rango.delivery.feature.payment

import androidx.compose.ui.text.AnnotatedString
import com.rango.delivery.data.payment.CreateCardRequest
import com.rango.delivery.data.payment.PaymentMethodDto
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.payment.SelectedPayment
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
class PaymentViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsMethodsWithPixSelectedAndCashChange() = runTest {
        val viewModel = PaymentViewModel(FakePayments(), PaymentSelection())

        val state = viewModel.uiState.value
        assertEquals(4, state.options.size)
        assertEquals(1L, state.selectedId)
        assertEquals("Troco para R$ 100,00", state.options.last().hint)
        assertFalse(state.showCardForm)
    }

    @Test
    fun keepsPreviousSelection() = runTest {
        val selection = PaymentSelection().apply { select(SelectedPayment(3, "debit", "Cartão de débito", "Nubank •••• 8802")) }

        val viewModel = PaymentViewModel(FakePayments(), selection)

        assertEquals(3L, viewModel.uiState.value.selectedId)
        assertTrue(viewModel.uiState.value.showCardForm)
    }

    @Test
    fun confirmingCashSendsChangeFor() = runTest {
        val selection = PaymentSelection()
        val viewModel = PaymentViewModel(FakePayments(), selection)

        viewModel.onSelect(4)
        viewModel.onConfirm()

        assertEquals(100.0, selection.selected.value?.changeFor)
        assertEquals(PaymentEvent.Done, viewModel.events.first())
    }

    @Test
    fun confirmingSavedCardWithoutDataSkipsBackend() = runTest {
        val repository = FakePayments()
        val selection = PaymentSelection()
        val viewModel = PaymentViewModel(repository, selection)

        viewModel.onSelect(2)
        viewModel.onConfirm()

        assertNull(repository.created)
        assertEquals(2L, selection.selected.value?.methodId)
        assertNull(selection.selected.value?.changeFor)
    }

    @Test
    fun invalidCardShowsErrorsPerField() = runTest {
        val viewModel = PaymentViewModel(FakePayments(), PaymentSelection())
        viewModel.onSelect(2)
        viewModel.onCardChange(CardField.Number, "4111 1111 1111 1112")
        viewModel.onCardChange(CardField.ExpiryCvv, "13/30 123")

        viewModel.onConfirm()

        val errors = viewModel.uiState.value.card.errors
        assertEquals("Número do cartão inválido.", errors[CardField.Number])
        assertEquals("Informe o nome impresso no cartão.", errors[CardField.Holder])
        assertEquals("Validade ou CVV inválidos.", errors[CardField.ExpiryCvv])
    }

    @Test
    fun validCardIsSavedWithBrandAndLastDigits() = runTest {
        val repository = FakePayments()
        val selection = PaymentSelection()
        val viewModel = PaymentViewModel(repository, selection)
        viewModel.onSelect(2)
        viewModel.onCardChange(CardField.Number, "5555 5555 5555 4444")
        viewModel.onCardChange(CardField.Holder, "Marina Alves")
        viewModel.onCardChange(CardField.ExpiryCvv, "08/29 321")

        viewModel.onConfirm()

        assertEquals(CreateCardRequest("credit", "Mastercard", "4444"), repository.created)
        assertEquals(5L, selection.selected.value?.methodId)
        assertEquals(PaymentEvent.Done, viewModel.events.first())
    }

    @Test
    fun masksGroupCardNumberAndExpiry() {
        assertEquals("4111 1111 1111 1111", CardNumberTransformation.filter(AnnotatedString("4111111111111111")).text.text)
        assertEquals("08/29 321", ExpiryCvvTransformation.filter(AnnotatedString("0829321")).text.text)
        assertEquals("Visa", CardRules.brandOf("4111111111111111"))
        assertEquals("Elo", CardRules.brandOf("6362970000457013"))
    }

    private class FakePayments : PaymentRepository {
        var created: CreateCardRequest? = null

        override suspend fun list() = listOf(
            method(1, "pix", "PIX", "Aprovação imediata"),
            method(2, "credit", "Cartão de crédito", "Visa •••• 4410"),
            method(3, "debit", "Cartão de débito", "Nubank •••• 8802"),
            method(4, "cash", "Dinheiro", "Pagamento na entrega"),
        )

        override suspend fun createCard(request: CreateCardRequest): PaymentMethodDto {
            created = request
            return method(5, request.type, "Cartão de crédito", "${request.brand} •••• ${request.last4}")
        }

        private fun method(id: Long, type: String, label: String, detail: String) = PaymentMethodDto(id, type, label, detail)
    }
}
