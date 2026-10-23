package com.rango.delivery.data.auth

data class SignupData(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val passwordConfirmation: String,
)

interface AuthRepository {
    suspend fun register(data: SignupData): UserDto
}

class DefaultAuthRepository(
    private val api: AuthApi,
    private val sessionRepository: SessionRepository,
) : AuthRepository {

    override suspend fun register(data: SignupData): UserDto {
        val session = api.register(
            RegisterRequest(
                name = data.name.trim(),
                email = data.email.trim(),
                phone = data.phone.trim(),
                password = data.password,
                passwordConfirmation = data.passwordConfirmation,
                acceptTerms = true,
            ),
        )
        sessionRepository.save(session)
        return session.user
    }
}
