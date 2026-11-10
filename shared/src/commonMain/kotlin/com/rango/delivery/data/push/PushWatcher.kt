package com.rango.delivery.data.push

import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.notification.NotificationDto
import com.rango.delivery.data.notification.NotificationRepository
import com.rango.delivery.data.storage.SecureStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

data class PushMessage(val id: Long, val title: String, val body: String)

class PushWatcher(
    private val notificationRepository: NotificationRepository,
    private val sessionRepository: SessionRepository,
    private val storage: SecureStorage,
    private val notifier: LocalNotifier,
    private val intervalMillis: Long = POLL_INTERVAL_MS,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _banner = MutableStateFlow<PushMessage?>(null)
    val banner: StateFlow<PushMessage?> = _banner.asStateFlow()

    private val _openRequested = MutableStateFlow(false)
    val openRequested: StateFlow<Boolean> = _openRequested.asStateFlow()

    @Volatile
    var inForeground: Boolean = true

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch { run() }
    }

    private suspend fun run() {
        while (true) {
            if (sessionRepository.accessToken.value != null) poll()
            delay(intervalMillis)
        }
    }

    suspend fun poll() {
        val unread = try {
            notificationRepository.unread()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return
        }
        val seen = storage.get(KEY_SEEN)?.split(',')?.mapNotNull { it.toLongOrNull() }?.toSet()
        storage.put(KEY_SEEN, unread.joinToString(",") { it.id.toString() })
        if (seen == null) return
        unread.filter { it.id !in seen }.sortedBy { it.id }.forEach(::deliver)
    }

    fun dismissBanner() {
        _banner.value = null
    }

    fun openFromBanner() {
        _banner.value = null
        requestOpen()
    }

    fun requestOpen() {
        _openRequested.value = true
    }

    fun consumeOpen() {
        _openRequested.value = false
    }

    private fun deliver(notification: NotificationDto) {
        if (inForeground) {
            _banner.value = PushMessage(notification.id, notification.title, notification.body)
        } else {
            notifier.show(notification.id, notification.title, notification.body)
        }
    }

    private companion object {
        const val KEY_SEEN = "push_seen"
        const val POLL_INTERVAL_MS = 10_000L
    }
}
