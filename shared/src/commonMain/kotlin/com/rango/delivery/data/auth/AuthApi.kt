package com.rango.delivery.data.auth

import com.rango.delivery.core.network.apiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class AuthApi(private val client: HttpClient) {

    suspend fun refresh(refreshToken: String): AuthSessionDto = apiCall {
        client.post("/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
    }

    suspend fun login(request: LoginRequest): AuthSessionDto = apiCall {
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun register(request: RegisterRequest): AuthSessionDto = apiCall {
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val keepSignedIn: Boolean,
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val passwordConfirmation: String,
    val acceptTerms: Boolean,
    val keepSignedIn: Boolean = true,
)

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
