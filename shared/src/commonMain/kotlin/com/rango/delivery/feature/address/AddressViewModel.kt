package com.rango.delivery.feature.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.address.CreateAddressRequest
import com.rango.delivery.data.address.SavedAddressDto
import com.rango.delivery.data.auth.SessionExpiredException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddressItem(
    val id: Long,
    val label: String,
    val line: String,
    val cityLine: String,
    val mapImageUrl: String?,
)

enum class AddressField { Cep, Street, Complement, District }

data class AddressForm(
    val cep: String = "",
    val street: String = "",
    val complement: String = "",
    val district: String = "",
    val errors: Map<AddressField, String> = emptyMap(),
)

data class AddressUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val addresses: List<AddressItem> = emptyList(),
    val selectedId: Long? = null,
    val formOpen: Boolean = false,
    val form: AddressForm = AddressForm(),
    val saving: Boolean = false,
) {
    val mapImageUrl: String? get() = addresses.firstOrNull { it.id == selectedId }?.mapImageUrl ?: addresses.firstOrNull()?.mapImageUrl
}

sealed interface AddressEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : AddressEvent
    data object Done : AddressEvent
    data object SessionExpired : AddressEvent
}

class AddressViewModel(private val addressRepository: AddressRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddressUiState())
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddressEvent>(Channel.BUFFERED)
    val events: Flow<AddressEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                applyList(addressRepository.list())
            } catch (e: SessionExpiredException) {
                _events.send(AddressEvent.SessionExpired)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false, errorMessage = e.message) }
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false, errorMessage = "Não foi possível carregar os endereços.") }
            }
        }
    }

    fun onSelect(addressId: Long) {
        val previous = _uiState.value.selectedId
        if (previous == addressId) return
        _uiState.update { it.copy(selectedId = addressId) }
        viewModelScope.launch {
            try {
                addressRepository.select(addressId)
                toast("Endereço atualizado.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                _events.send(AddressEvent.SessionExpired)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(selectedId = previous) }
                toast("Não foi possível atualizar o endereço.", ToastKind.Error)
            }
        }
    }

    fun onToggleForm() {
        _uiState.update { it.copy(formOpen = !it.formOpen) }
    }

    fun onFieldChange(field: AddressField, value: String) {
        _uiState.update { state ->
            val form = state.form
            val updated = when (field) {
                AddressField.Cep -> form.copy(cep = value.filter { it.isDigit() || it == '-' }.take(9))
                AddressField.Street -> form.copy(street = value)
                AddressField.Complement -> form.copy(complement = value)
                AddressField.District -> form.copy(district = value)
            }
            state.copy(form = updated.copy(errors = updated.errors - field))
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.saving) return
        val (neighborhood, city) = splitDistrict(state.form.district)
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                addressRepository.create(
                    CreateAddressRequest(
                        label = NEW_ADDRESS_LABEL,
                        cep = state.form.cep,
                        street = state.form.street.trim(),
                        complement = state.form.complement.trim().ifEmpty { null },
                        neighborhood = neighborhood,
                        city = city,
                        select = true,
                    ),
                )
                applyList(addressRepository.list())
                _uiState.update { it.copy(formOpen = false, form = AddressForm()) }
                toast("Endereço salvo no dispositivo.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                _events.send(AddressEvent.SessionExpired)
            } catch (e: ApiException) {
                val errors = buildMap {
                    e.details.forEach { detail -> fieldFor(detail.field)?.let { putIfAbsent(it, detail.message) } }
                }
                if (errors.isEmpty()) toast(e.message, ToastKind.Error)
                _uiState.update { it.copy(form = it.form.copy(errors = errors)) }
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            } finally {
                _uiState.update { it.copy(saving = false) }
            }
        }
    }

    fun onUseAddress() {
        viewModelScope.launch { _events.send(AddressEvent.Done) }
    }

    private fun applyList(list: List<SavedAddressDto>) {
        _uiState.update { state ->
            state.copy(
                loading = false,
                errorMessage = null,
                addresses = list.map { AddressItem(it.id, it.label, it.line, it.cityLine, it.mapImageUrl?.let { url -> apiBaseUrl + url }) },
                selectedId = list.firstOrNull { it.isSelected }?.id ?: state.selectedId,
            )
        }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(AddressEvent.ShowToast(message, kind)) }
    }

    private companion object {
        const val NEW_ADDRESS_LABEL = "Outro"

        fun fieldFor(apiField: String): AddressField? = when (apiField) {
            "cep" -> AddressField.Cep
            "street" -> AddressField.Street
            "complement" -> AddressField.Complement
            "neighborhood", "city" -> AddressField.District
            else -> null
        }

        fun splitDistrict(value: String): Pair<String, String> {
            val separator = listOf(" · ", ",", " - ").firstOrNull { value.contains(it) } ?: return value.trim() to ""
            return value.substringBeforeLast(separator).trim() to value.substringAfterLast(separator).trim()
        }
    }
}
