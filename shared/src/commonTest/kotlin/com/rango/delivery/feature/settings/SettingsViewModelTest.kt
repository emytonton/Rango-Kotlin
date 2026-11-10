package com.rango.delivery.feature.settings

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.AuthSessionDto
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.cart.CartDto
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.cart.CartSummaryDto
import com.rango.delivery.data.catalog.ProductSummaryDto
import com.rango.delivery.data.connection.ConnectionRepository
import com.rango.delivery.data.favorite.FavoriteRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.payment.SelectedPayment
import com.rango.delivery.data.profile.NotificationPrefsDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.storage.InMemoryStorage
import com.rango.delivery.data.theme.ThemeRepository
import com.rango.delivery.data.profile.UpdateProfileRequest
import com.rango.delivery.data.sync.DataSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val now = Instant.fromEpochMilliseconds(1_762_700_000_000)

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsThemeAndConnection() = runTest {
        val state = Fixture(theme = "dark").viewModel(this).uiState.value

        assertEquals("Escuro", state.themeLabel)
        assertEquals(true, state.online)
        assertEquals("conectado", state.connectionHint)
        assertEquals("ainda não sincronizado nesta sessão", state.syncHint(now))
    }

    @Test
    fun offlineServerShowsWarning() = runTest {
        val fixture = Fixture(online = false)
        val viewModel = fixture.viewModel(this)
        assertEquals("sem conexão com o servidor", viewModel.uiState.value.connectionHint)

        viewModel.onConnection()

        assertEquals(SettingsEvent.ShowToast("Sem conexão com o servidor.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun connectionRestoredAfterOffline() = runTest {
        val fixture = Fixture(online = false)
        val viewModel = fixture.viewModel(this)
        fixture.connection.online = true

        viewModel.onConnection()

        assertEquals(true, viewModel.uiState.value.online)
        assertEquals(SettingsEvent.ShowToast("Conexão restaurada.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun syncFetchesAccountDataAndRecordsTime() = runTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel(this)
        fixture.profile.theme = "light"

        viewModel.onSync()

        val state = viewModel.uiState.value
        assertFalse(state.syncing)
        assertEquals("Claro", state.themeLabel)
        assertEquals(now, state.lastSyncedAt)
        assertEquals(1, fixture.favorites.lists)
        assertEquals(1, fixture.cart.loads)
        assertEquals(SettingsEvent.ShowToast("Dados sincronizados.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun syncWithoutNetworkMarksOffline() = runTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel(this)
        fixture.cart.failure = NetworkException(RuntimeException())

        viewModel.onSync()

        assertEquals(false, viewModel.uiState.value.online)
        assertNull(viewModel.uiState.value.lastSyncedAt)
        assertEquals(
            SettingsEvent.ShowToast("Não foi possível conectar ao servidor.", ToastKind.Error),
            viewModel.events.first(),
        )
    }

    @Test
    fun clearLocalDataLogsOutAndResetsMemory() = runTest {
        val fixture = Fixture()
        fixture.payment.select(SelectedPayment(1, "pix", "PIX", ""))
        val viewModel = fixture.viewModel(this)
        viewModel.onSync()
        viewModel.events.first()

        viewModel.onClearLocalData()

        assertEquals(1, fixture.session.logouts)
        assertEquals(1, fixture.favorites.clears)
        assertNull(fixture.payment.selected.value)
        assertNull(viewModel.uiState.value.lastSyncedAt)
        assertEquals(
            listOf(SettingsEvent.ShowToast("Dados locais apagados.", ToastKind.Warning), SettingsEvent.LocalDataCleared),
            viewModel.events.take(2).toList(),
        )
    }

    @Test
    fun expiredSessionNavigatesAway() = runTest {
        val fixture = Fixture()
        fixture.profile.failure = SessionExpiredException()

        assertEquals(SettingsEvent.SessionExpired, fixture.viewModel(this).events.first())
    }

    @Test
    fun aboutRowsShowToasts() = runTest {
        val viewModel = Fixture().viewModel(this)

        viewModel.onLanguage()
        viewModel.onHelp()
        viewModel.onTerms()

        assertEquals(
            listOf(
                SettingsEvent.ShowToast("Apenas português neste protótipo.", ToastKind.Warning),
                SettingsEvent.ShowToast("Central de ajuda em breve.", ToastKind.Warning),
                SettingsEvent.ShowToast("Documentos abertos no navegador.", ToastKind.Warning),
            ),
            viewModel.events.take(3).toList(),
        )
    }

    @Test
    fun lastSyncLabel() {
        assertEquals("última sincronização: agora", formatLastSync(now - 30.seconds, now))
        assertEquals("última sincronização: há 8 min", formatLastSync(now - 8.minutes, now))
        assertEquals("última sincronização: há 2 h", formatLastSync(now - 2.hours - 5.minutes, now))
        assertTrue(formatLastSync(null, now).startsWith("ainda"))
    }

    private inner class Fixture(theme: String = "system", online: Boolean = true) {
        val profile = FakeProfile(theme)
        val connection = FakeConnection(online)
        val favorites = FakeFavorites()
        val cart = FakeCart()
        val session = FakeSession()
        val payment = PaymentSelection()
        val theme = ThemeRepository(InMemoryStorage(), profile)

        fun viewModel(scope: TestScope): SettingsViewModel {
            Dispatchers.setMain(UnconfinedTestDispatcher(scope.testScheduler))
            return SettingsViewModel(
                profileRepository = profile,
                connectionRepository = connection,
                dataSync = DataSync(profile, favorites, cart, theme) { now },
                sessionRepository = session,
                favoriteRepository = favorites,
                paymentSelection = payment,
                themeRepository = theme,
            )
        }
    }

    private class FakeProfile(var theme: String) : ProfileRepository {
        var failure: Exception? = null

        override suspend fun me(): UserDto = error("não usado")

        override suspend fun settings(): SettingsDto {
            failure?.let { throw it }
            return SettingsDto(theme)
        }

        override suspend fun unreadNotifications() = 0
        override suspend fun updateTheme(theme: String) = SettingsDto(theme)
        override suspend fun updateNotifications(prefs: NotificationPrefsDto) = SettingsDto(notifications = prefs)
        override suspend fun update(request: UpdateProfileRequest): UserDto = error("não usado")
        override suspend fun uploadAvatar(jpeg: ByteArray): UserDto = error("não usado")
        override suspend fun removeAvatar(): UserDto = error("não usado")
    }

    private class FakeConnection(var online: Boolean) : ConnectionRepository {
        override suspend fun isOnline() = online
    }

    private class FakeFavorites : FavoriteRepository {
        var lists = 0
        var clears = 0
        override val favoriteIds: StateFlow<Set<String>> = MutableStateFlow(emptySet())

        override suspend fun list(): List<ProductSummaryDto> {
            lists++
            return emptyList()
        }

        override suspend fun setFavorite(productId: String, favorite: Boolean) = favorite

        override fun clearLocal() {
            clears++
        }
    }

    private class FakeCart : CartRepository {
        var loads = 0
        var failure: Exception? = null

        override suspend fun cart(): CartDto {
            failure?.let { throw it }
            loads++
            return CartDto(summary = CartSummaryDto(itemCount = 0))
        }

        override suspend fun addItem(productId: String, quantity: Int, addonIds: List<String>, notes: String) = 0
        override suspend fun updateQuantity(itemId: Long, quantity: Int): CartDto = error("não usado")
        override suspend fun removeItem(itemId: Long): CartDto = error("não usado")
        override suspend fun clear(): CartDto = error("não usado")
        override suspend fun applyCoupon(code: String): CartDto = error("não usado")
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
