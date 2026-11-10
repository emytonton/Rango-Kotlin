package com.rango.delivery.feature.notifications

import com.rango.delivery.core.designsystem.component.NotificationTone
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.notification.NotificationDto
import com.rango.delivery.data.notification.NotificationPageDto
import com.rango.delivery.data.notification.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsNotificationsWithKindAndRelativeTime() = runTest {
        val state = viewModel(FakeNotifications()).uiState.value

        assertTrue(state.loaded)
        assertEquals(listOf("há 4 min", "há 1 h", "ontem"), state.items.map { it.time })
        assertEquals(listOf(NotificationTone.Order, NotificationTone.Promo, NotificationTone.Order), state.items.map { it.tone })
        assertEquals("2 não lidas", state.unreadLabel)
    }

    @Test
    fun openingUnreadOrderMarksAsReadAndOpensTracking() = runTest {
        val repository = FakeNotifications()
        val viewModel = viewModel(repository)

        viewModel.onOpen(1)

        assertEquals(listOf(1L), repository.read)
        assertFalse(viewModel.uiState.value.items.first { it.id == 1L }.unread)
        assertEquals(NotificationsEvent.OpenOrder("RG-10482"), viewModel.events.first())
    }

    @Test
    fun openingPromoOnlyMarksAsRead() = runTest {
        val repository = FakeNotifications()
        val viewModel = viewModel(repository)

        viewModel.onOpen(2)

        assertEquals(listOf(2L), repository.read)
        assertEquals("1 não lidas", viewModel.uiState.value.unreadLabel)
    }

    @Test
    fun failedMarkReadRestoresUnread() = runTest {
        val repository = FakeNotifications(failure = NetworkException(RuntimeException()))
        val viewModel = viewModel(repository)

        viewModel.onOpen(2)

        assertTrue(viewModel.uiState.value.items.first { it.id == 2L }.unread)
    }

    @Test
    fun readAllClearsUnreadAndShowsToast() = runTest {
        val repository = FakeNotifications()
        val viewModel = viewModel(repository)

        viewModel.onReadAll()

        assertEquals(1, repository.readAllCalls)
        assertEquals("tudo lido", viewModel.uiState.value.unreadLabel)
        assertEquals(
            NotificationsEvent.ShowToast("Notificações marcadas como lidas.", ToastKind.Success),
            viewModel.events.first(),
        )
    }

    @Test
    fun loadErrorShowsMessageAndRetryRecovers() = runTest {
        val repository = FakeNotifications(listFailure = NetworkException(RuntimeException()))
        val viewModel = viewModel(repository)

        assertEquals("Não foi possível carregar as notificações.", viewModel.uiState.value.errorMessage)

        repository.listFailure = null
        viewModel.retry()

        assertEquals(3, viewModel.uiState.value.items.size)
    }

    @Test
    fun expiredSessionNavigatesAway() = runTest {
        val viewModel = viewModel(FakeNotifications(listFailure = SessionExpiredException()))

        assertEquals(NotificationsEvent.SessionExpired, viewModel.events.first())
    }

    private fun TestScope.viewModel(repository: NotificationRepository): NotificationsViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return NotificationsViewModel(repository, clock = { NOW }, timeZone = TimeZone.UTC)
    }

    private class FakeNotifications(
        var failure: Exception? = null,
        var listFailure: Exception? = null,
    ) : NotificationRepository {
        val read = mutableListOf<Long>()
        var readAllCalls = 0

        override suspend fun list(): NotificationPageDto {
            listFailure?.let { throw it }
            return NotificationPageDto(NOTIFICATIONS, unreadCount = 2)
        }

        override suspend fun unread(): List<NotificationDto> = NOTIFICATIONS.filter { !it.isRead }

        override suspend fun markRead(id: Long): NotificationDto {
            failure?.let { throw it }
            read += id
            return NOTIFICATIONS.first { it.id == id }.copy(isRead = true)
        }

        override suspend fun markAllRead(): Int {
            failure?.let { throw it }
            readAllCalls++
            return 0
        }
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-11-10T12:00:00Z")
        val NOTIFICATIONS = listOf(
            NotificationDto(1, "order", "Seu pedido saiu para entrega", "Pedido RG-10482", 10482, "RG-10482", false, "2026-11-10T11:56:00.000Z"),
            NotificationDto(2, "promo", "Tem promoção nova esperando por você", "20% off", null, null, false, "2026-11-10T11:00:00.000Z"),
            NotificationDto(3, "order", "Seu pedido chegou!", "Pedido RG-10391", 10391, "RG-10391", true, "2026-11-09T20:00:00.000Z"),
        )
    }
}
