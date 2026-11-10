package com.rango.delivery.data.push

import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.AuthSessionDto
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import com.rango.delivery.data.notification.NotificationDto
import com.rango.delivery.data.notification.NotificationPageDto
import com.rango.delivery.data.notification.NotificationRepository
import com.rango.delivery.data.storage.InMemoryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PushWatcherTest {

    @Test
    fun firstPollOnlyRemembersExistingUnread() = runTest {
        val notifier = FakeNotifier()
        val watcher = watcher(FakeNotifications(listOf(notification(1))), notifier)

        watcher.poll()

        assertNull(watcher.banner.value)
        assertTrue(notifier.shown.isEmpty())
    }

    @Test
    fun newUnreadInForegroundShowsBanner() = runTest {
        val repository = FakeNotifications(listOf(notification(1)))
        val watcher = watcher(repository, FakeNotifier())
        watcher.poll()

        repository.unread = listOf(notification(1), notification(2, "Seu pedido saiu para entrega"))
        watcher.poll()

        assertEquals("Seu pedido saiu para entrega", watcher.banner.value?.title)
    }

    @Test
    fun newUnreadInBackgroundShowsLocalNotificationOnce() = runTest {
        val repository = FakeNotifications(emptyList())
        val notifier = FakeNotifier()
        val watcher = watcher(repository, notifier)
        watcher.poll()
        watcher.inForeground = false

        repository.unread = listOf(notification(5))
        watcher.poll()
        watcher.poll()

        assertEquals(listOf(5L), notifier.shown)
        assertNull(watcher.banner.value)
    }

    @Test
    fun networkErrorKeepsSeenIds() = runTest {
        val repository = FakeNotifications(listOf(notification(1)))
        val notifier = FakeNotifier()
        val watcher = watcher(repository, notifier)
        watcher.poll()
        watcher.inForeground = false

        repository.failure = NetworkException(RuntimeException())
        watcher.poll()
        repository.failure = null
        watcher.poll()

        assertTrue(notifier.shown.isEmpty())
    }

    @Test
    fun tappingBannerRequestsNotificationsScreen() = runTest {
        val repository = FakeNotifications(emptyList())
        val watcher = watcher(repository, FakeNotifier())
        watcher.poll()
        repository.unread = listOf(notification(7))
        watcher.poll()

        watcher.openFromBanner()

        assertNull(watcher.banner.value)
        assertTrue(watcher.openRequested.value)
        watcher.consumeOpen()
        assertFalse(watcher.openRequested.value)
    }

    @Test
    fun signingOutForgetsSeenIdsSoTheNextAccountStartsClean() = runTest {
        val session = FakeSession()
        val repository = FakeNotifications(listOf(notification(1)))
        val notifier = FakeNotifier()
        val watcher = PushWatcher(repository, session, InMemoryStorage(), notifier, intervalMillis = 1_000, scope = backgroundScope)
        watcher.inForeground = false
        watcher.start()
        runCurrent()

        session.token.value = null
        advanceTimeBy(1_001)
        repository.unread = listOf(notification(1), notification(9))
        session.token.value = "outra conta"
        advanceTimeBy(1_000)

        assertTrue(notifier.shown.isEmpty())
    }

    @Test
    fun localMessageUsesBannerInForegroundAndSystemInBackground() {
        val notifier = FakeNotifier()
        val watcher = watcher(FakeNotifications(emptyList()), notifier)
        val message = PushMessage(900_001, "Notificação de teste", "Tudo certo.")

        watcher.show(message)
        assertEquals(message, watcher.banner.value)
        assertTrue(notifier.shown.isEmpty())

        watcher.dismissBanner()
        watcher.inForeground = false
        watcher.show(message)
        assertNull(watcher.banner.value)
        assertEquals(listOf(900_001L), notifier.shown)
    }

    private fun watcher(repository: NotificationRepository, notifier: LocalNotifier) =
        PushWatcher(repository, FakeSession(), InMemoryStorage(), notifier)

    private fun notification(id: Long, title: String = "Seu pedido foi confirmado") =
        NotificationDto(id, "order", title, "Pedido RG-$id", id, "RG-$id", false, "2026-11-10T12:00:00.000Z")

    private class FakeNotifier : LocalNotifier {
        val shown = mutableListOf<Long>()

        override fun show(id: Long, title: String, body: String) {
            shown += id
        }
    }

    private class FakeNotifications(var unread: List<NotificationDto>) : NotificationRepository {
        var failure: Exception? = null

        override suspend fun list() = NotificationPageDto(unread, unread.size)

        override suspend fun unread(): List<NotificationDto> {
            failure?.let { throw it }
            return unread
        }

        override suspend fun markRead(id: Long): NotificationDto = error("não usado")
        override suspend fun markAllRead(): Int = 0
    }

    private class FakeSession : SessionRepository {
        val token = MutableStateFlow<String?>("token")
        override val accessToken: StateFlow<String?> = token
        override suspend fun restore(): SessionRestore = SessionRestore.None
        override suspend fun renewAccessToken(expiredToken: String?): String? = null
        override fun save(session: AuthSessionDto) = Unit
        override fun clear() = Unit
        override suspend fun logout() = Unit
    }
}
