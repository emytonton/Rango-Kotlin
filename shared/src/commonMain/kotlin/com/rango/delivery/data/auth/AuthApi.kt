package com.rango.delivery.data.auth

import com.rango.delivery.core.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class AuthApi(private val client: HttpClient) {

    suspend fun refresh(refreshToken: String): AuthSessionDto = apiCall {
        client.post("/auth/refresh") { setBody(RefreshRequest(refreshToken)) }.body()
    }
}

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
    val refreshTokenExpiresAt: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String = "",
    val avatarUrl: String? = null,
)
