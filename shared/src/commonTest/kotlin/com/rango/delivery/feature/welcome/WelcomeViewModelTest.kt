package com.rango.delivery.feature.welcome

import com.rango.delivery.data.auth.AuthSessionDto
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun withoutSavedSessionShowsWelcome() = runTest {
        val viewModel = WelcomeViewModel(FakeSessionRepository(SessionRestore.None))

        assertEquals(WelcomeUiState.Ready(savedSessionName = null), viewModel.uiState.value)
    }

    @Test
    fun restoredSessionEmitsNavigationEvent() = runTest {
        val viewModel = WelcomeViewModel(FakeSessionRepository(SessionRestore.Restored("Marina Alves")))

        assertEquals(WelcomeEvent.SessionRestored, viewModel.events.first())
        assertEquals(WelcomeUiState.CheckingSession, viewModel.uiState.value)
    }

    @Test
    fun unreachableBackendKeepsSavedSessionBanner() = runTest {
        val viewModel = WelcomeViewModel(FakeSessionRepository(SessionRestore.Unreachable("Marina Alves")))

        assertEquals(WelcomeUiState.Ready(savedSessionName = "Marina Alves"), viewModel.uiState.value)
    }

    private class FakeSessionRepository(private val result: SessionRestore) : SessionRepository {
        override val accessToken: StateFlow<String?> = MutableStateFlow(null)
        override suspend fun restore(): SessionRestore = result
        override fun save(session: AuthSessionDto) = Unit
        override fun clear() = Unit
    }
}
