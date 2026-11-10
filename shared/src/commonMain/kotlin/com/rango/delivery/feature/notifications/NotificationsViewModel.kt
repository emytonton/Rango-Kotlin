package com.rango.delivery.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.NotificationTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.format.formatRelativeTime
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.notification.NotificationDto
import com.rango.delivery.data.notification.NotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

data class NotificationItem(
    val id: Long,
    val title: String,
    val text: String,
    val time: String,
    val tone: NotificationTone,
    val unread: Boolean,
    val orderCode: String?,
)

data class NotificationsUiState(
    val loading: Boolean = true,
    val loaded: Boolean = false,
    val errorMessage: String? = null,
    val items: List<NotificationItem> = emptyList(),
) {
    val unreadCount: Int get() = items.count { it.unread }
    val unreadLabel: String get() = if (unreadCount > 0) "$unreadCount não lidas" else "tudo lido"
    val empty: Boolean get() = loaded && errorMessage == null && items.isEmpty()
}

sealed interface NotificationsEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : NotificationsEvent
    data class OpenOrder(val orderCode: String) : NotificationsEvent
    data object SessionExpired : NotificationsEvent
}

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val clock: () -> Instant = { Clock.System.now() },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun refresh() {
        if (_uiState.value.loaded) load()
    }

    fun onOpen(id: Long) {
        val item = _uiState.value.items.firstOrNull { it.id == id } ?: return
        if (item.unread) {
            setUnread(setOf(id), unread = false)
            viewModelScope.launch {
                try {
                    notificationRepository.markRead(id)
                } catch (e: SessionExpiredException) {
                    _events.send(NotificationsEvent.SessionExpired)
                } catch (e: ApiException) {
                    setUnread(setOf(id), unread = true)
                } catch (e: NetworkException) {
                    setUnread(setOf(id), unread = true)
                }
            }
        }
        item.orderCode?.let { code -> viewModelScope.launch { _events.send(NotificationsEvent.OpenOrder(code)) } }
    }

    fun onReadAll() {
        val unreadIds = _uiState.value.items.filter { it.unread }.map { it.id }.toSet()
        setUnread(unreadIds, unread = false)
        viewModelScope.launch {
            try {
                notificationRepository.markAllRead()
                _events.send(NotificationsEvent.ShowToast("Notificações marcadas como lidas.", ToastKind.Success))
            } catch (e: SessionExpiredException) {
                _events.send(NotificationsEvent.SessionExpired)
            } catch (e: ApiException) {
                failReadAll(unreadIds)
            } catch (e: NetworkException) {
                failReadAll(unreadIds)
            }
        }
    }

    private suspend fun failReadAll(ids: Set<Long>) {
        setUnread(ids, unread = true)
        _events.send(NotificationsEvent.ShowToast("Não foi possível marcar as notificações.", ToastKind.Error))
    }

    private fun setUnread(ids: Set<Long>, unread: Boolean) {
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id in ids) it.copy(unread = unread) else it })
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                val now = clock()
                val items = notificationRepository.list().data.map { it.toItem(now) }
                _uiState.update { it.copy(loading = false, loaded = true, items = items) }
            } catch (e: SessionExpiredException) {
                _events.send(NotificationsEvent.SessionExpired)
            } catch (e: ApiException) {
                fail(e.message)
            } catch (e: NetworkException) {
                fail("Não foi possível carregar as notificações.")
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { if (it.loaded) it.copy(loading = false) else it.copy(loading = false, errorMessage = message) }
    }

    private fun NotificationDto.toItem(now: Instant) = NotificationItem(
        id = id,
        title = title,
        text = body,
        time = formatRelativeTime(createdAt, now, timeZone),
        tone = when (kind) {
            "promo" -> NotificationTone.Promo
            "news" -> NotificationTone.News
            else -> NotificationTone.Order
        },
        unread = !isRead,
        orderCode = orderCode,
    )
}
