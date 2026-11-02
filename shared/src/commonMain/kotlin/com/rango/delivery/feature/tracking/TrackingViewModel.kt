package com.rango.delivery.feature.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.TimelineState
import com.rango.delivery.core.format.formatDeliveryTime
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.order.OrderDetailDto
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.order.TimelineStepDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

const val TRACKING_POLL_MILLIS = 5_000L

data class TrackingStep(val step: Int, val label: String, val time: String, val state: TimelineState)

data class TrackingCourier(val name: String, val vehicle: String, val photoUrl: String?)

data class TrackingOrder(
    val code: String,
    val step: Int,
    val cancelled: Boolean,
    val label: String,
    val hint: String,
    val eta: String,
    val steps: List<TrackingStep>,
    val courier: TrackingCourier?,
    val mapImageUrl: String?,
)

data class TrackingUiState(
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val order: TrackingOrder? = null,
)

sealed interface TrackingEvent {
    data object SessionExpired : TrackingEvent
}

class TrackingViewModel(
    private val orderCode: String,
    private val orderRepository: OrderRepository,
    private val pollMillis: Long = TRACKING_POLL_MILLIS,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private val _events = Channel<TrackingEvent>(Channel.BUFFERED)
    val events: Flow<TrackingEvent> = _events.receiveAsFlow()

    private var polling: Job? = null

    fun startPolling() {
        if (polling?.isActive == true) return
        polling = viewModelScope.launch {
            while (refresh()) delay(pollMillis)
        }
    }

    fun stopPolling() {
        polling?.cancel()
        polling = null
    }

    fun retry() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        startPolling()
    }

    private suspend fun refresh(): Boolean {
        try {
            val order = orderRepository.detail(orderCode)
            _uiState.update { it.copy(loading = false, errorMessage = null, order = order.toTracking()) }
            return order.status == "live"
        } catch (e: SessionExpiredException) {
            _events.send(TrackingEvent.SessionExpired)
            return false
        } catch (e: ApiException) {
            return keepPolling(e.message)
        } catch (e: NetworkException) {
            return keepPolling("Não foi possível carregar o pedido.")
        }
    }

    private fun keepPolling(message: String?): Boolean {
        if (_uiState.value.order != null) return true
        _uiState.update { it.copy(loading = false, errorMessage = message) }
        return false
    }

    private fun OrderDetailDto.toTracking() = TrackingOrder(
        code = code,
        step = step,
        cancelled = status == "cancel",
        label = statusLabel,
        hint = hint,
        eta = when (status) {
            "done" -> "Entregue"
            "cancel" -> "Cancelado"
            else -> formatDeliveryTime(estimatedDelivery.minMinutes, estimatedDelivery.maxMinutes)
        },
        steps = timeline.map { it.toStep() },
        courier = courier?.let { TrackingCourier(it.name, it.vehicle, it.photoUrl?.let { url -> apiBaseUrl + url }) },
        mapImageUrl = mapImageUrl?.let { apiBaseUrl + it },
    )

    private fun TimelineStepDto.toStep(): TrackingStep {
        val state = when (state) {
            "done" -> TimelineState.Done
            "current" -> TimelineState.Current
            else -> TimelineState.Upcoming
        }
        val time = when (state) {
            TimelineState.Done -> reachedAt?.let(::clock) ?: ""
            TimelineState.Current -> "agora"
            TimelineState.Upcoming -> "previsto"
        }
        return TrackingStep(step, label, time, state)
    }

    private fun clock(iso: String): String {
        val local = Instant.parse(iso).toLocalDateTime(timeZone)
        return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    }
}
