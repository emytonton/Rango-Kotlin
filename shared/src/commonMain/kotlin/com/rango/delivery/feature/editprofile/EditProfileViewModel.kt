package com.rango.delivery.feature.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.image.PhotoPickResult
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.UserDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.UpdateProfileRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val loading: Boolean = true,
    val loaded: Boolean = false,
    val errorMessage: String? = null,
    val form: EditProfileForm = EditProfileForm(),
    val submitted: Boolean = false,
    val saving: Boolean = false,
    val serverErrors: Map<ProfileField, String> = emptyMap(),
    val avatarUrl: String? = null,
    val photoSheetVisible: Boolean = false,
    val cropSource: ByteArray? = null,
    val uploadingPhoto: Boolean = false,
) {
    val errors: Map<ProfileField, String>
        get() = (if (submitted) form.validate() else emptyMap()) + serverErrors

    val initials: String
        get() = form.name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}

sealed interface EditProfileEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : EditProfileEvent
    data object Saved : EditProfileEvent
    data object SessionExpired : EditProfileEvent
}

class EditProfileViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events: Flow<EditProfileEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            runRequest(onFailure = { message -> _uiState.update { it.copy(loading = false, errorMessage = message) } }) {
                val user = profileRepository.me()
                _uiState.update {
                    it.copy(
                        loading = false,
                        loaded = true,
                        form = EditProfileForm(user.name, user.email, user.phone),
                        avatarUrl = user.avatarUrl.toAbsoluteUrl(),
                    )
                }
            }
        }
    }

    fun onFieldChange(field: ProfileField, value: String) {
        _uiState.update { it.copy(form = it.form.with(field, value), serverErrors = it.serverErrors - field) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.loaded || state.saving) return
        _uiState.update { it.copy(submitted = true) }
        if (state.form.validate().isNotEmpty()) return
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                val form = state.form
                profileRepository.update(UpdateProfileRequest(form.name.trim(), form.email.trim(), form.phone.trim()))
                _events.send(EditProfileEvent.ShowToast("Perfil atualizado com sucesso.", ToastKind.Success))
                _events.send(EditProfileEvent.Saved)
            } catch (e: SessionExpiredException) {
                _events.send(EditProfileEvent.SessionExpired)
            } catch (e: ApiException) {
                val fieldErrors = e.details.mapNotNull { detail ->
                    ProfileField.entries.firstOrNull { it.apiName == detail.field }?.let { it to detail.message }
                }.toMap()
                _uiState.update { it.copy(serverErrors = fieldErrors) }
                if (fieldErrors.isEmpty()) toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            } finally {
                _uiState.update { it.copy(saving = false) }
            }
        }
    }

    fun onOpenPhoto() {
        val state = _uiState.value
        if (state.loaded && !state.uploadingPhoto) _uiState.update { it.copy(photoSheetVisible = true) }
    }

    fun onClosePhoto() {
        _uiState.update { it.copy(photoSheetVisible = false) }
    }

    fun onPhotoResult(result: PhotoPickResult) {
        when (result) {
            is PhotoPickResult.Picked -> _uiState.update { it.copy(cropSource = result.bytes) }
            PhotoPickResult.PermissionDenied -> toast("Permita o acesso à câmera para tirar a foto.", ToastKind.Warning)
            PhotoPickResult.Cancelled -> Unit
        }
    }

    fun onCropCancel() {
        _uiState.update { it.copy(cropSource = null) }
    }

    fun onCropConfirm(jpeg: ByteArray?) {
        _uiState.update { it.copy(cropSource = null) }
        if (jpeg == null || jpeg.isEmpty()) {
            toast("Não foi possível ler a imagem.", ToastKind.Error)
            return
        }
        updateAvatar("Foto atualizada.", ToastKind.Success) { profileRepository.uploadAvatar(jpeg) }
    }

    fun onRemovePhoto() {
        _uiState.update { it.copy(photoSheetVisible = false) }
        updateAvatar("Foto removida.", ToastKind.Warning) { profileRepository.removeAvatar() }
    }

    private fun updateAvatar(successMessage: String, kind: ToastKind, request: suspend () -> UserDto) {
        if (_uiState.value.uploadingPhoto) return
        _uiState.update { it.copy(uploadingPhoto = true) }
        viewModelScope.launch {
            runRequest(onFailure = { toast(it, ToastKind.Error) }) {
                val user = request()
                _uiState.update { it.copy(avatarUrl = user.avatarUrl.toAbsoluteUrl()) }
                toast(successMessage, kind)
            }
            _uiState.update { it.copy(uploadingPhoto = false) }
        }
    }

    private suspend fun runRequest(onFailure: (String) -> Unit, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: SessionExpiredException) {
            _events.send(EditProfileEvent.SessionExpired)
        } catch (e: ApiException) {
            onFailure(e.message)
        } catch (e: NetworkException) {
            onFailure(e.message.orEmpty())
        }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(EditProfileEvent.ShowToast(message, kind)) }
    }

    private fun String?.toAbsoluteUrl(): String? = this?.let { apiBaseUrl + it }
}
