package com.rango.delivery.data.notification

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long,
    val kind: String,
    val title: String,
    val body: String,
    val orderId: Long? = null,
    val orderCode: String? = null,
    val isRead: Boolean,
    val createdAt: String,
)

@Serializable
data class NotificationPageDto(val data: List<NotificationDto>, val unreadCount: Int = 0)

@Serializable
data class ReadAllResultDto(val unreadCount: Int = 0)

const val NOTIFICATION_PAGE_SIZE = 50

class NotificationApi(private val client: HttpClient) {

    suspend fun list(token: String, unreadOnly: Boolean): NotificationPageDto = apiCall {
        client.get("/notifications") {
            bearerAuth(token)
            parameter("unreadOnly", unreadOnly)
            parameter("pageSize", NOTIFICATION_PAGE_SIZE)
        }.body()
    }

    suspend fun markRead(token: String, id: Long): NotificationDto = apiCall {
        client.post("/notifications/$id/read") { bearerAuth(token) }.body()
    }

    suspend fun markAllRead(token: String): ReadAllResultDto = apiCall {
        client.post("/notifications/read-all") { bearerAuth(token) }.body()
    }
}

interface NotificationRepository {
    suspend fun list(): NotificationPageDto
    suspend fun unread(): List<NotificationDto>
    suspend fun markRead(id: Long): NotificationDto
    suspend fun markAllRead(): Int
}

class DefaultNotificationRepository(
    private val api: NotificationApi,
    private val caller: AuthorizedCaller,
) : NotificationRepository {
    override suspend fun list(): NotificationPageDto = caller.call { api.list(it, unreadOnly = false) }

    override suspend fun unread(): List<NotificationDto> = caller.call { api.list(it, unreadOnly = true) }.data

    override suspend fun markRead(id: Long): NotificationDto = caller.call { api.markRead(it, id) }

    override suspend fun markAllRead(): Int = caller.call { api.markAllRead(it) }.unreadCount
}
