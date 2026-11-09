package com.rango.delivery.data.profile

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.auth.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(val theme: String = "system")

@Serializable
data class NotificationCountDto(val unreadCount: Int = 0)

class ProfileApi(private val client: HttpClient) {

    suspend fun me(token: String): UserDto = apiCall {
        client.get("/me") { bearerAuth(token) }.body()
    }

    suspend fun settings(token: String): SettingsDto = apiCall {
        client.get("/me/settings") { bearerAuth(token) }.body()
    }

    suspend fun unreadNotifications(token: String): Int = apiCall {
        client.get("/notifications") {
            bearerAuth(token)
            parameter("pageSize", 1)
        }.body<NotificationCountDto>().unreadCount
    }
}

interface ProfileRepository {
    suspend fun me(): UserDto
    suspend fun settings(): SettingsDto
    suspend fun unreadNotifications(): Int
}

class DefaultProfileRepository(
    private val api: ProfileApi,
    private val caller: AuthorizedCaller,
) : ProfileRepository {
    override suspend fun me(): UserDto = caller.call { api.me(it) }

    override suspend fun settings(): SettingsDto = caller.call { api.settings(it) }

    override suspend fun unreadNotifications(): Int = caller.call { api.unreadNotifications(it) }
}
