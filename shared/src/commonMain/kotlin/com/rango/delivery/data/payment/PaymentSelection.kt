package com.rango.delivery.data.payment

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SelectedPayment(
    val methodId: Long,
    val type: String,
    val label: String,
    val detail: String,
    val changeFor: Double? = null,
)

class PaymentSelection {
    private val _selected = MutableStateFlow<SelectedPayment?>(null)
    val selected: StateFlow<SelectedPayment?> = _selected.asStateFlow()

    fun select(payment: SelectedPayment) {
        _selected.value = payment
    }
}
