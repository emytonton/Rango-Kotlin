package com.rango.delivery.feature.notificationsettings

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.profile.NotificationPrefsDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.profile.UpdateProfileRequest
import com.rango.delivery.data.push.PushMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsPreferencesFromBackend() = runTest {
        val viewModel = viewModel(FakeProfile(NotificationPrefsDto(orders = true, promos = false, news = true)))

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertTrue(state.isOn(NotificationPref.Orders))
        assertFalse(state.isOn(NotificationPref.Promos))
        assertTrue(state.isOn(NotificationPref.News))
    }

    @Test
    fun toggleSendsAllPreferencesToBackend() = runTest {
        val profile = FakeProfile(NotificationPrefsDto(orders = true, promos = true, news = false))
        val viewModel = viewModel(profile)

        viewModel.onToggle(NotificationPref.News)

        assertEquals(listOf(NotificationPrefsDto(orders = true, promos = true, news = true)), profile.sent)
        assertTrue(viewModel.uiState.value.isOn(NotificationPref.News))
    }

    @Test
    fun failedToggleRevertsAndShowsToast() = runTest {
        val profile = FakeProfile(NotificationPrefsDto(orders = true, promos = true, news = false))
        val viewModel = viewModel(profile)
        profile.updateFailure = NetworkException(RuntimeException())

        viewModel.onToggle(NotificationPref.Orders)

        assertTrue(viewModel.uiState.value.isOn(NotificationPref.Orders))
        val event = viewModel.events.first() as NotificationSettingsEvent.ShowToast
        assertEquals(ToastKind.Error, event.kind)
    }

    @Test
    fun loadErrorShowsMessageAndRetryRecovers() = runTest {
        val profile = FakeProfile(loadFailure = ApiException(500, "INTERNAL", "Erro no servidor."))
        val viewModel = viewModel(profile)

        assertEquals("Erro no servidor.", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.prefs)

        profile.loadFailure = null
        viewModel.retry()

        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isOn(NotificationPref.Orders))
    }

    @Test
    fun expiredSessionLeavesTheScreen() = runTest {
        val viewModel = viewModel(FakeProfile(loadFailure = SessionExpiredException()))

        assertEquals(NotificationSettingsEvent.SessionExpired, viewModel.events.first())
    }

    @Test
    fun testPushShowsMessage() = runTest {
        val shown = mutableListOf<PushMessage>()
        val viewModel = viewModel(FakeProfile(), shown::add)

        viewModel.sendTest()

        assertEquals("Notificação de teste", shown.single().title)
    }

    private fun TestScope.viewModel(
        profile: FakeProfile,
        showPush: (PushMessage) -> Unit = {},
    ): NotificationSettingsViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return NotificationSettingsViewModel(profile, showPush)
    }

    private class FakeProfile(
        private val prefs: NotificationPrefsDto = NotificationPrefsDto(orders = true, promos = true, news = false),
        var loadFailure: Exception? = null,
    ) : ProfileRepository {
        var updateFailure: Exception? = null
        val sent = mutableListOf<NotificationPrefsDto>()

        override suspend fun settings(): SettingsDto {
            loadFailure?.let { throw it }
            return SettingsDto(notifications = prefs)
        }

        override suspend fun updateNotifications(prefs: NotificationPrefsDto): SettingsDto {
            updateFailure?.let { throw it }
            sent += prefs
            return SettingsDto(notifications = prefs)
        }

        override suspend fun me(): UserDto = error("não usado")
        override suspend fun updateTheme(theme: String) = SettingsDto(theme)
        override suspend fun unreadNotifications() = 0
        override suspend fun update(request: UpdateProfileRequest): UserDto = error("não usado")
        override suspend fun uploadAvatar(jpeg: ByteArray): UserDto = error("não usado")
        override suspend fun removeAvatar(): UserDto = error("não usado")
    }
}
