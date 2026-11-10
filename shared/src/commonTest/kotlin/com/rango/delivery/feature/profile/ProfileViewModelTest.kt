package com.rango.delivery.feature.profile

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.address.CreateAddressRequest
import com.rango.delivery.data.address.SavedAddressDto
import com.rango.delivery.data.auth.AuthSessionDto
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.payment.CreateCardRequest
import com.rango.delivery.data.payment.PaymentMethodDto
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.storage.InMemoryStorage
import com.rango.delivery.data.theme.ThemeRepository
import com.rango.delivery.data.profile.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAccountAndShortcutHints() = runTest {
        val state = viewModel(FakeProfile()).uiState.value

        assertTrue(state.loaded)
        assertEquals("Marina Alves", state.name)
        assertEquals("MA", state.initials)
        assertEquals("marina.alves@email.com", state.email)
        assertEquals("(11) 98876-4410", state.phone)
        assertNull(state.avatarUrl)
        assertEquals("2 salvos", state.addressHint)
        assertEquals("4 métodos disponíveis", state.paymentHint)
        assertEquals("24 não lidas", state.notificationsHint)
        assertEquals("Claro", state.themeLabel)
    }

    @Test
    fun noUnreadShowsAllRead() = runTest {
        val state = viewModel(FakeProfile(unread = 0, theme = "system")).uiState.value

        assertEquals("tudo lido", state.notificationsHint)
        assertEquals("Sistema", state.themeLabel)
    }

    @Test
    fun avatarUrlUsesApiBase() = runTest {
        val state = viewModel(FakeProfile(avatarUrl = "/static/avatars/1.jpg")).uiState.value

        assertTrue(state.avatarUrl!!.endsWith("/static/avatars/1.jpg"))
    }

    @Test
    fun errorWithRetry() = runTest {
        val profile = FakeProfile(failure = ApiException(500, "INTERNAL", "Erro no servidor."))
        val viewModel = viewModel(profile)
        assertEquals("Erro no servidor.", viewModel.uiState.value.errorMessage)

        profile.failure = null
        viewModel.retry()
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("Marina Alves", viewModel.uiState.value.name)
    }

    @Test
    fun expiredSessionNavigatesAway() = runTest {
        val viewModel = viewModel(FakeProfile(failure = SessionExpiredException()))

        assertEquals(ProfileEvent.SessionExpired, viewModel.events.first())
    }

    @Test
    fun helpShowsWarning() = runTest {
        val viewModel = viewModel(FakeProfile())

        viewModel.onHelp()

        assertEquals(ProfileEvent.ShowToast("Central de ajuda em breve.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun logoutEndsSession() = runTest {
        val session = FakeSession()
        val viewModel = viewModel(FakeProfile(), session)

        viewModel.onLogout()

        assertEquals(1, session.logouts)
        assertEquals(ProfileEvent.LoggedOut, viewModel.events.first())
    }

    private fun TestScope.viewModel(profile: ProfileRepository, session: FakeSession = FakeSession()): ProfileViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return ProfileViewModel(profile, FakeAddresses(), FakePayments(), session, ThemeRepository(InMemoryStorage(), profile))
    }

    private class FakeProfile(
        private val unread: Int = 24,
        private val theme: String = "light",
        private val avatarUrl: String? = null,
        var failure: Exception? = null,
    ) : ProfileRepository {
        override suspend fun me(): UserDto {
            failure?.let { throw it }
            return UserDto(1, "Marina Alves", "marina.alves@email.com", "(11) 98876-4410", avatarUrl)
        }

        override suspend fun settings() = SettingsDto(theme)

        override suspend fun unreadNotifications() = unread
        override suspend fun updateTheme(theme: String) = SettingsDto(theme)

        override suspend fun update(request: UpdateProfileRequest): UserDto = error("não usado")

        override suspend fun uploadAvatar(jpeg: ByteArray): UserDto = error("não usado")

        override suspend fun removeAvatar(): UserDto = error("não usado")
    }

    private class FakeAddresses : AddressRepository {
        override suspend fun list() = listOf(address(1), address(2))
        override suspend fun create(request: CreateAddressRequest): SavedAddressDto = error("não usado")
        override suspend fun select(addressId: Long): SavedAddressDto = error("não usado")

        private fun address(id: Long) = SavedAddressDto(
            id = id,
            label = "Casa",
            cep = "05422-030",
            street = "Rua das Palmeiras, 128",
            neighborhood = "Pinheiros",
            city = "São Paulo",
            line = "Rua das Palmeiras, 128",
            cityLine = "Pinheiros · São Paulo",
            isSelected = id == 1L,
        )
    }

    private class FakePayments : PaymentRepository {
        override suspend fun list() = (1L..4L).map { PaymentMethodDto(it, "pix", "PIX", "") }
        override suspend fun createCard(request: CreateCardRequest): PaymentMethodDto = error("não usado")
    }

    private class FakeSession : SessionRepository {
        var logouts = 0
        override val accessToken: StateFlow<String?> = MutableStateFlow("token")
        override suspend fun restore(): SessionRestore = SessionRestore.None
        override suspend fun renewAccessToken(expiredToken: String?): String? = null
        override fun save(session: AuthSessionDto) = Unit
        override fun clear() = Unit
        override suspend fun logout() {
            logouts++
        }
    }
}
