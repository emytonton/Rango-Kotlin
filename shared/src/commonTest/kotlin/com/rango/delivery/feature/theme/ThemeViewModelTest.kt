package com.rango.delivery.feature.theme

import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.profile.UpdateProfileRequest
import com.rango.delivery.data.storage.InMemoryStorage
import com.rango.delivery.data.theme.ThemeKeys
import com.rango.delivery.data.theme.ThemeRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsWithSavedThemeOrSystem() = runTest {
        assertEquals(ThemeKeys.SYSTEM, viewModel(ThemeRepository(InMemoryStorage(), FakeProfile())).uiState.value.theme)

        val saved = ThemeRepository(InMemoryStorage(mapOf("theme" to "dark")), FakeProfile())
        assertEquals(ThemeKeys.DARK, viewModel(saved).uiState.value.theme)
    }

    @Test
    fun selectSavesOnDeviceAndSendsToBackend() = runTest {
        val storage = InMemoryStorage()
        val profile = FakeProfile()
        val repository = ThemeRepository(storage, profile)
        val viewModel = viewModel(repository)

        viewModel.onSelect(ThemeKeys.DARK)

        assertEquals(ThemeKeys.DARK, viewModel.uiState.value.theme)
        assertEquals("dark", storage.get("theme"))
        assertEquals(listOf("dark"), profile.sent)
        assertFalse(repository.hasPendingSync)
    }

    @Test
    fun offlineChoiceStaysPendingAndIsSentLater() = runTest {
        val profile = FakeProfile(failure = NetworkException(RuntimeException()))
        val repository = ThemeRepository(InMemoryStorage(), profile)
        viewModel(repository).onSelect(ThemeKeys.LIGHT)

        assertEquals(ThemeKeys.LIGHT, repository.theme.value)
        assertTrue(repository.hasPendingSync)

        profile.failure = null
        viewModel(repository)

        assertEquals(listOf("light"), profile.sent)
        assertFalse(repository.hasPendingSync)
    }

    @Test
    fun remoteThemeIsIgnoredWhileLocalChoiceIsPending() {
        val profile = FakeProfile(failure = NetworkException(RuntimeException()))
        val repository = ThemeRepository(InMemoryStorage(mapOf("theme" to "dark", "theme_pending" to "dark")), profile)

        repository.onRemote(ThemeKeys.LIGHT)
        assertEquals(ThemeKeys.DARK, repository.theme.value)

        val synced = ThemeRepository(InMemoryStorage(), profile)
        synced.onRemote(ThemeKeys.LIGHT)
        assertEquals(ThemeKeys.LIGHT, synced.theme.value)
    }

    @Test
    fun previewHighlightFollowsChoiceAndDevice() {
        assertTrue(ThemeUiState(ThemeKeys.LIGHT).isPreviewActive(previewDark = false, systemDark = true))
        assertFalse(ThemeUiState(ThemeKeys.LIGHT).isPreviewActive(previewDark = true, systemDark = true))
        assertTrue(ThemeUiState(ThemeKeys.DARK).isPreviewActive(previewDark = true, systemDark = false))
        assertTrue(ThemeUiState(ThemeKeys.SYSTEM).isPreviewActive(previewDark = true, systemDark = true))
        assertFalse(ThemeUiState(ThemeKeys.SYSTEM).isPreviewActive(previewDark = false, systemDark = true))
    }

    @Test
    fun expiredSessionNavigatesAway() = runTest {
        val profile = FakeProfile(failure = SessionExpiredException())
        val viewModel = viewModel(ThemeRepository(InMemoryStorage(), profile))

        viewModel.onSelect(ThemeKeys.DARK)

        assertEquals(ThemeEvent.SessionExpired, viewModel.events.first())
    }

    private fun TestScope.viewModel(repository: ThemeRepository): ThemeViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return ThemeViewModel(repository)
    }

    private class FakeProfile(var failure: Exception? = null) : ProfileRepository {
        val sent = mutableListOf<String>()

        override suspend fun updateTheme(theme: String): SettingsDto {
            failure?.let { throw it }
            sent += theme
            return SettingsDto(theme)
        }

        override suspend fun me(): UserDto = error("não usado")
        override suspend fun settings() = SettingsDto()
        override suspend fun unreadNotifications() = 0
        override suspend fun update(request: UpdateProfileRequest): UserDto = error("não usado")
        override suspend fun uploadAvatar(jpeg: ByteArray): UserDto = error("não usado")
        override suspend fun removeAvatar(): UserDto = error("não usado")
    }
}
