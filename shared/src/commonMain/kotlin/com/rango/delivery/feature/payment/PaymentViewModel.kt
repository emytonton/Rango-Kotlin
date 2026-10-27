package com.rango.delivery.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatPrice
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.payment.CreateCardRequest
import com.rango.delivery.data.payment.PaymentMethodDto
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.payment.SelectedPayment
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentType { Pix, Credit, Debit, Cash }

data class PaymentOption(
    val id: Long,
    val type: PaymentType,
    val label: String,
    val hint: String,
)

enum class CardField { Number, Holder, ExpiryCvv }

data class CardForm(
    val number: String = "",
    val holder: String = "",
    val expiryCvv: String = "",
    val errors: Map<CardField, String> = emptyMap(),
) {
    val isBlank: Boolean get() = number.isEmpty() && holder.isBlank() && expiryCvv.isEmpty()
}

data class PaymentUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val options: List<PaymentOption> = emptyList(),
    val selectedId: Long? = null,
    val card: CardForm = CardForm(),
    val saving: Boolean = false,
) {
    val selected: PaymentOption? get() = options.firstOrNull { it.id == selectedId }
    val showCardForm: Boolean get() = selected?.type == PaymentType.Credit || selected?.type == PaymentType.Debit
}

sealed interface PaymentEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : PaymentEvent
    data object Done : PaymentEvent
    data object SessionExpired : PaymentEvent
}

class PaymentViewModel(
    private val paymentRepository: PaymentRepository,
    private val selection: PaymentSelection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _events = Channel<PaymentEvent>(Channel.BUFFERED)
    val events: Flow<PaymentEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val options = paymentRepository.list().mapNotNull { it.toOption() }
                val current = selection.selected.value?.methodId?.takeIf { id -> options.any { it.id == id } }
                _uiState.update {
                    it.copy(
                        loading = false,
                        options = options,
                        selectedId = current ?: options.firstOrNull { o -> o.type == PaymentType.Pix }?.id ?: options.firstOrNull()?.id,
                    )
                }
            } catch (e: SessionExpiredException) {
                _events.send(PaymentEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar as formas de pagamento.") }
            }
        }
    }

    fun onSelect(id: Long) {
        _uiState.update { it.copy(selectedId = id) }
    }

    fun onCardChange(field: CardField, value: String) {
        _uiState.update { state ->
            val card = state.card
            val updated = when (field) {
                CardField.Number -> card.copy(number = value.filter(Char::isDigit).take(CARD_NUMBER_MAX))
                CardField.Holder -> card.copy(holder = value)
                CardField.ExpiryCvv -> card.copy(expiryCvv = value.filter(Char::isDigit).take(EXPIRY_CVV_MAX))
            }
            state.copy(card = updated.copy(errors = updated.errors - field))
        }
    }

    fun onConfirm() {
        val state = _uiState.value
        val option = state.selected ?: return
        if (state.saving) return
        if (!state.showCardForm || state.card.isBlank) {
            confirm(option)
            return
        }
        val errors = validate(state.card)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(card = it.card.copy(errors = errors)) }
            return
        }
        saveCard(option.type, state.card)
    }

    private fun saveCard(type: PaymentType, card: CardForm) {
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                val created = paymentRepository.createCard(
                    CreateCardRequest(
                        type = if (type == PaymentType.Credit) "credit" else "debit",
                        brand = CardRules.brandOf(card.number),
                        last4 = card.number.takeLast(4),
                    ),
                )
                created.toOption()?.let(::confirm)
            } catch (e: SessionExpiredException) {
                _events.send(PaymentEvent.SessionExpired)
            } catch (e: ApiException) {
                _events.send(PaymentEvent.ShowToast(e.message, ToastKind.Error))
            } catch (e: NetworkException) {
                _events.send(PaymentEvent.ShowToast(e.message.orEmpty(), ToastKind.Error))
            } finally {
                _uiState.update { it.copy(saving = false) }
            }
        }
    }

    private fun confirm(option: PaymentOption) {
        selection.select(
            SelectedPayment(
                methodId = option.id,
                type = option.type.name.lowercase(),
                label = option.label,
                detail = option.hint,
                changeFor = if (option.type == PaymentType.Cash) CASH_CHANGE_FOR else null,
            ),
        )
        viewModelScope.launch { _events.send(PaymentEvent.Done) }
    }

    private fun validate(card: CardForm): Map<CardField, String> = buildMap {
        if (!CardRules.isValidNumber(card.number)) put(CardField.Number, "Número do cartão inválido.")
        if (card.holder.isBlank()) put(CardField.Holder, "Informe o nome impresso no cartão.")
        if (!CardRules.isValidExpiryCvv(card.expiryCvv)) put(CardField.ExpiryCvv, "Validade ou CVV inválidos.")
    }

    private companion object {
        const val CASH_CHANGE_FOR = 100.0

        fun PaymentMethodDto.toOption(): PaymentOption? {
            val type = when (type) {
                "pix" -> PaymentType.Pix
                "credit" -> PaymentType.Credit
                "debit" -> PaymentType.Debit
                "cash" -> PaymentType.Cash
                else -> return null
            }
            val hint = if (type == PaymentType.Cash) "Troco para ${formatPrice(CASH_CHANGE_FOR)}" else detail
            return PaymentOption(id, type, label, hint)
        }
    }
}
