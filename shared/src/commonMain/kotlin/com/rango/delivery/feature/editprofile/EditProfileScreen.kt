package com.rango.delivery.feature.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoActionSheet
import com.rango.delivery.core.designsystem.component.RangoEditableAvatar
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSheetAction
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTextField
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.core.image.rememberPhotoPicker
import com.rango.delivery.feature.editprofile.components.PhotoCropOverlay
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.edit_profile_cancel
import com.rango.delivery.resources.edit_profile_change_photo
import com.rango.delivery.resources.edit_profile_email
import com.rango.delivery.resources.edit_profile_name
import com.rango.delivery.resources.edit_profile_phone
import com.rango.delivery.resources.edit_profile_photo_title
import com.rango.delivery.resources.edit_profile_pick_gallery
import com.rango.delivery.resources.edit_profile_remove_photo
import com.rango.delivery.resources.edit_profile_retry
import com.rango.delivery.resources.edit_profile_save
import com.rango.delivery.resources.edit_profile_save_changes
import com.rango.delivery.resources.edit_profile_saving
import com.rango.delivery.resources.edit_profile_take_photo
import com.rango.delivery.resources.edit_profile_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class EditProfileActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onFieldChange: (ProfileField, String) -> Unit = { _, _ -> },
    val onSave: () -> Unit = {},
    val onOpenPhoto: () -> Unit = {},
    val onClosePhoto: () -> Unit = {},
    val onTakePhoto: () -> Unit = {},
    val onPickFromGallery: () -> Unit = {},
    val onRemovePhoto: () -> Unit = {},
    val onCropCancel: () -> Unit = {},
    val onCropConfirm: (ByteArray?) -> Unit = {},
)

private data class FieldSpec(
    val field: ProfileField,
    val label: StringResource,
    val icon: ImageVector,
    val keyboardType: KeyboardType,
)

private val Fields = listOf(
    FieldSpec(ProfileField.Name, Res.string.edit_profile_name, RangoIcons.Person, KeyboardType.Text),
    FieldSpec(ProfileField.Email, Res.string.edit_profile_email, RangoIcons.Mail, KeyboardType.Email),
    FieldSpec(ProfileField.Phone, Res.string.edit_profile_phone, RangoIcons.Call, KeyboardType.Phone),
)

@Composable
fun EditProfileRoute(
    viewModel: EditProfileViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val picker = rememberPhotoPicker(onResult = viewModel::onPhotoResult)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EditProfileEvent.ShowToast -> toast.show(event.message, event.kind)
                EditProfileEvent.Saved -> onBack()
                EditProfileEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    EditProfileScreen(
        state = state,
        actions = EditProfileActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onFieldChange = viewModel::onFieldChange,
            onSave = viewModel::onSave,
            onOpenPhoto = viewModel::onOpenPhoto,
            onClosePhoto = viewModel::onClosePhoto,
            onTakePhoto = {
                viewModel.onClosePhoto()
                picker.takePhoto()
            },
            onPickFromGallery = {
                viewModel.onClosePhoto()
                picker.pickFromGallery()
            },
            onRemovePhoto = viewModel::onRemovePhoto,
            onCropCancel = viewModel::onCropCancel,
            onCropConfirm = viewModel::onCropConfirm,
        ),
    )
}

@Composable
fun EditProfileScreen(state: EditProfileUiState, actions: EditProfileActions) {
    val colors = Rango.colors
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.surface).imePadding()) {
            RangoTopBar(title = stringResource(Res.string.edit_profile_title), onBack = actions.onBack) {
                Text(
                    text = stringResource(Res.string.edit_profile_save),
                    style = Rango.typography.body.copy(fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold),
                    color = colors.primary,
                    modifier = Modifier.clickable(enabled = state.loaded, onClick = actions.onSave).padding(vertical = 6.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                if (state.errorMessage != null) {
                    RangoErrorState(
                        message = state.errorMessage,
                        actionText = stringResource(Res.string.edit_profile_retry),
                        onAction = actions.onRetry,
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 18.dp),
                    )
                } else {
                    EditProfileContent(state = state, actions = actions)
                }
            }
            Column(modifier = Modifier.background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
                HorizontalDivider(thickness = 1.dp, color = colors.border)
                RangoPrimaryButton(
                    text = stringResource(Res.string.edit_profile_save_changes),
                    onClick = actions.onSave,
                    enabled = state.loaded,
                    loading = state.saving,
                    loadingText = stringResource(Res.string.edit_profile_saving),
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 20.dp),
                )
            }
        }
        if (state.photoSheetVisible) {
            RangoActionSheet(
                title = stringResource(Res.string.edit_profile_photo_title),
                actions = listOf(
                    RangoSheetAction(RangoIcons.PhotoCamera, stringResource(Res.string.edit_profile_take_photo), actions.onTakePhoto),
                    RangoSheetAction(RangoIcons.PhotoLibrary, stringResource(Res.string.edit_profile_pick_gallery), actions.onPickFromGallery),
                    RangoSheetAction(RangoIcons.Delete, stringResource(Res.string.edit_profile_remove_photo), actions.onRemovePhoto, danger = true),
                ),
                cancelText = stringResource(Res.string.edit_profile_cancel),
                onDismiss = actions.onClosePhoto,
            )
        }
        state.cropSource?.let { source ->
            PhotoCropOverlay(source = source, onCancel = actions.onCropCancel, onConfirm = actions.onCropConfirm)
        }
    }
}

@Composable
private fun EditProfileContent(state: EditProfileUiState, actions: EditProfileActions) {
    val colors = Rango.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.loading && !state.loaded) {
                RangoSkeleton(Modifier.size(106.dp), cornerRadius = 34.dp)
            } else {
                RangoEditableAvatar(
                    initials = state.initials,
                    imageUrl = state.avatarUrl,
                    onClick = actions.onOpenPhoto,
                    loading = state.uploadingPhoto,
                    contentDescription = stringResource(Res.string.edit_profile_change_photo),
                )
            }
            Text(
                text = stringResource(Res.string.edit_profile_change_photo),
                style = Rango.typography.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.clickable(onClick = actions.onOpenPhoto).padding(4.dp),
            )
        }
        Fields.forEachIndexed { index, spec ->
            if (state.loading && !state.loaded) {
                RangoSkeleton(Modifier.fillMaxWidth().height(78.dp), cornerRadius = 15.dp)
            } else {
                val last = index == Fields.lastIndex
                val label = stringResource(spec.label)
                RangoTextField(
                    label = label,
                    value = state.form.valueOf(spec.field),
                    onValueChange = { actions.onFieldChange(spec.field, it) },
                    leadingIcon = spec.icon,
                    placeholder = label,
                    error = state.errors[spec.field],
                    keyboardType = spec.keyboardType,
                    imeAction = if (last) ImeAction.Done else ImeAction.Next,
                    keyboardActions = if (last) KeyboardActions(onDone = { actions.onSave() }) else KeyboardActions.Default,
                )
            }
        }
    }
}

private val PreviewState = EditProfileUiState(
    loading = false,
    loaded = true,
    form = EditProfileForm(name = "Marina Alves", email = "marina.alves@email.com", phone = "(11) 98876-4410"),
)

@Preview
@Composable
private fun EditProfileScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        EditProfileScreen(PreviewState, EditProfileActions())
    }
}

@Preview
@Composable
private fun EditProfileScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        EditProfileScreen(PreviewState.copy(submitted = true, form = PreviewState.form.copy(email = "marina@")), EditProfileActions())
    }
}
