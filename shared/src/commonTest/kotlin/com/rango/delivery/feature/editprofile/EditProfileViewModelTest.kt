package com.rango.delivery.feature.editprofile

import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.image.PhotoPickResult
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.ApiFieldError
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.profile.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsCurrentData() = runTest {
        val state = viewModel(FakeProfile(avatarUrl = "/uploads/avatars/1.jpg")).uiState.value

        assertTrue(state.loaded)
        assertEquals(EditProfileForm("Marina Alves", "marina.alves@email.com", "(11) 98876-4410"), state.form)
        assertEquals("MA", state.initials)
        assertTrue(state.avatarUrl!!.endsWith("/uploads/avatars/1.jpg"))
    }

    @Test
    fun invalidFieldsBlockSave() = runTest {
        val profile = FakeProfile()
        val viewModel = viewModel(profile)
        viewModel.onFieldChange(ProfileField.Email, "marina@")
        viewModel.onFieldChange(ProfileField.Phone, "1199")

        viewModel.onSave()

        val errors = viewModel.uiState.value.errors
        assertEquals("E-mail inválido.", errors[ProfileField.Email])
        assertEquals("Telefone incompleto.", errors[ProfileField.Phone])
        assertNull(profile.updated)
    }

    @Test
    fun saveSendsTrimmedDataAndGoesBack() = runTest {
        val profile = FakeProfile()
        val viewModel = viewModel(profile)
        viewModel.onFieldChange(ProfileField.Name, "  Marina Souza ")

        viewModel.onSave()

        assertEquals(UpdateProfileRequest("Marina Souza", "marina.alves@email.com", "(11) 98876-4410"), profile.updated)
        val events = viewModel.events.take(2).toList()
        assertEquals(EditProfileEvent.ShowToast("Perfil atualizado com sucesso.", ToastKind.Success), events[0])
        assertEquals(EditProfileEvent.Saved, events[1])
    }

    @Test
    fun emailInUseMarksField() = runTest {
        val profile = FakeProfile(
            updateFailure = ApiException(
                409,
                "EMAIL_IN_USE",
                "Este e-mail já está cadastrado.",
                listOf(ApiFieldError("email", "Este e-mail já está cadastrado.")),
            ),
        )
        val viewModel = viewModel(profile)

        viewModel.onSave()

        assertEquals("Este e-mail já está cadastrado.", viewModel.uiState.value.errors[ProfileField.Email])
        viewModel.onFieldChange(ProfileField.Email, "novo@email.com")
        assertNull(viewModel.uiState.value.errors[ProfileField.Email])
    }

    @Test
    fun pickedPhotoOpensCropAndUploads() = runTest {
        val profile = FakeProfile()
        val viewModel = viewModel(profile)
        viewModel.onOpenPhoto()
        assertTrue(viewModel.uiState.value.photoSheetVisible)
        viewModel.onClosePhoto()

        viewModel.onPhotoResult(PhotoPickResult.Picked(byteArrayOf(1, 2, 3)))
        assertTrue(viewModel.uiState.value.cropSource != null)

        viewModel.onCropConfirm(byteArrayOf(9, 9))

        val state = viewModel.uiState.value
        assertNull(state.cropSource)
        assertFalse(state.uploadingPhoto)
        assertEquals(2, profile.uploaded?.size)
        assertTrue(state.avatarUrl!!.endsWith("/uploads/avatars/novo.jpg"))
        assertEquals(EditProfileEvent.ShowToast("Foto atualizada.", ToastKind.Success), viewModel.events.first())
    }

    @Test
    fun uploadErrorShowsToast() = runTest {
        val profile = FakeProfile(uploadFailure = ApiException(413, "FILE_TOO_LARGE", "Imagem muito grande (máximo 5 MB)."))
        val viewModel = viewModel(profile)

        viewModel.onCropConfirm(byteArrayOf(1))

        assertNull(viewModel.uiState.value.avatarUrl)
        assertEquals(
            EditProfileEvent.ShowToast("Imagem muito grande (máximo 5 MB).", ToastKind.Error),
            viewModel.events.first(),
        )
    }

    @Test
    fun removePhotoClearsAvatar() = runTest {
        val viewModel = viewModel(FakeProfile(avatarUrl = "/uploads/avatars/1.jpg"))
        viewModel.onOpenPhoto()

        viewModel.onRemovePhoto()

        assertFalse(viewModel.uiState.value.photoSheetVisible)
        assertNull(viewModel.uiState.value.avatarUrl)
        assertEquals(EditProfileEvent.ShowToast("Foto removida.", ToastKind.Warning), viewModel.events.first())
    }

    @Test
    fun deniedCameraShowsWarning() = runTest {
        val viewModel = viewModel(FakeProfile())

        viewModel.onPhotoResult(PhotoPickResult.PermissionDenied)

        assertNull(viewModel.uiState.value.cropSource)
        assertEquals(
            EditProfileEvent.ShowToast("Permita o acesso à câmera para tirar a foto.", ToastKind.Warning),
            viewModel.events.first(),
        )
    }

    @Test
    fun expiredSessionNavigatesAway() = runTest {
        val viewModel = viewModel(FakeProfile(loadFailure = SessionExpiredException()))

        assertEquals(EditProfileEvent.SessionExpired, viewModel.events.first())
    }

    @Test
    fun loadErrorWithRetry() = runTest {
        val profile = FakeProfile(loadFailure = ApiException(500, "INTERNAL", "Erro no servidor."))
        val viewModel = viewModel(profile)
        assertEquals("Erro no servidor.", viewModel.uiState.value.errorMessage)

        profile.loadFailure = null
        viewModel.load()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("Marina Alves", viewModel.uiState.value.form.name)
    }

    private fun TestScope.viewModel(profile: ProfileRepository): EditProfileViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return EditProfileViewModel(profile)
    }

    private class FakeProfile(
        private val avatarUrl: String? = null,
        var loadFailure: Exception? = null,
        private val updateFailure: Exception? = null,
        private val uploadFailure: Exception? = null,
    ) : ProfileRepository {
        var updated: UpdateProfileRequest? = null
        var uploaded: ByteArray? = null

        override suspend fun me(): UserDto {
            loadFailure?.let { throw it }
            return user(avatarUrl)
        }

        override suspend fun settings() = SettingsDto()

        override suspend fun unreadNotifications() = 0

        override suspend fun update(request: UpdateProfileRequest): UserDto {
            updateFailure?.let { throw it }
            updated = request
            return user(avatarUrl).copy(name = request.name, email = request.email, phone = request.phone)
        }

        override suspend fun uploadAvatar(jpeg: ByteArray): UserDto {
            uploadFailure?.let { throw it }
            uploaded = jpeg
            return user("/uploads/avatars/novo.jpg")
        }

        override suspend fun removeAvatar(): UserDto = user(null)

        private fun user(avatar: String?) =
            UserDto(1, "Marina Alves", "marina.alves@email.com", "(11) 98876-4410", avatar)
    }
}
