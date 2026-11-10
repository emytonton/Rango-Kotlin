package com.rango.delivery.data.profile

import com.rango.delivery.core.network.apiCall
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.auth.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(val theme: String = "system")

@Serializable
data class NotificationCountDto(val unreadCount: Int = 0)

@Serializable
data class UpdateSettingsRequest(val theme: String)

@Serializable
data class UpdateProfileRequest(val name: String, val email: String, val phone: String)

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

    suspend fun updateSettings(token: String, request: UpdateSettingsRequest): SettingsDto = apiCall {
        client.patch("/me/settings") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun update(token: String, request: UpdateProfileRequest): UserDto = apiCall {
        client.patch("/me") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun uploadAvatar(token: String, jpeg: ByteArray): UserDto = apiCall {
        client.put("/me/avatar") {
            bearerAuth(token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            "file",
                            jpeg,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                            },
                        )
                    },
                ),
            )
        }.body()
    }

    suspend fun removeAvatar(token: String): UserDto = apiCall {
        client.delete("/me/avatar") { bearerAuth(token) }.body()
    }
}

interface ProfileRepository {
    suspend fun me(): UserDto
    suspend fun settings(): SettingsDto
    suspend fun updateTheme(theme: String): SettingsDto
    suspend fun unreadNotifications(): Int
    suspend fun update(request: UpdateProfileRequest): UserDto
    suspend fun uploadAvatar(jpeg: ByteArray): UserDto
    suspend fun removeAvatar(): UserDto
}

class DefaultProfileRepository(
    private val api: ProfileApi,
    private val caller: AuthorizedCaller,
) : ProfileRepository {
    override suspend fun me(): UserDto = caller.call { api.me(it) }

    override suspend fun settings(): SettingsDto = caller.call { api.settings(it) }

    override suspend fun updateTheme(theme: String): SettingsDto =
        caller.call { api.updateSettings(it, UpdateSettingsRequest(theme)) }

    override suspend fun unreadNotifications(): Int = caller.call { api.unreadNotifications(it) }

    override suspend fun update(request: UpdateProfileRequest): UserDto = caller.call { api.update(it, request) }

    override suspend fun uploadAvatar(jpeg: ByteArray): UserDto = caller.call { api.uploadAvatar(it, jpeg) }

    override suspend fun removeAvatar(): UserDto = caller.call { api.removeAvatar(it) }
}
