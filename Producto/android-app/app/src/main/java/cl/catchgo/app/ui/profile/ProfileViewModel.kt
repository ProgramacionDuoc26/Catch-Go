package cl.catchgo.app.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.repository.AuthRepository
import cl.catchgo.app.domain.repository.HabilidadesRepository
import cl.catchgo.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val radar: RadarData? = null,
    val habilidades: List<HabilidadUsuario> = emptyList(),
    val photoUrl: String? = null,
    val cvUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val isUploadingCv: Boolean = false,
    val hasLocation: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habilidadesRepository: HabilidadesRepository,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: String = ""
    private var currentProfile: ProfileRemoteDto = ProfileRemoteDto()

    fun loadProfile(userId: String) {
        this.userId = userId
        viewModelScope.launch {
            profileRepository.getProfile(userId).onSuccess { profile ->
                if (profile != null) {
                    currentProfile = profile
                    _uiState.update {
                        it.copy(
                            photoUrl = profile.photoUrl,
                            cvUrl = profile.cvUrl,
                            hasLocation = profile.latitude != null && profile.longitude != null
                        )
                    }
                }
            }
        }
    }

    fun loadSkills(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val radar = habilidadesRepository.getRadar(userId).getOrNull()
            val skills = habilidadesRepository.getHabilidadesUsuario(userId).getOrDefault(emptyList())
            _uiState.update { it.copy(isLoading = false, radar = radar, habilidades = skills) }
        }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes == null) {
                _uiState.update { it.copy(isUploadingPhoto = false) }
                return@launch
            }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png")) ".png" else ".jpg"
            profileRepository.uploadFile(userId, bytes, "photo$ext", mimeType)
                .onSuccess { url ->
                    val updated = currentProfile.copy(userId = userId, photoUrl = url)
                    profileRepository.saveProfile(updated)
                        .onSuccess { saved ->
                            currentProfile = saved
                            _uiState.update { it.copy(isUploadingPhoto = false, photoUrl = url) }
                        }
                        .onFailure { _uiState.update { it.copy(isUploadingPhoto = false) } }
                }
                .onFailure { _uiState.update { it.copy(isUploadingPhoto = false) } }
        }
    }

    fun onCvPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCv = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes == null) {
                _uiState.update { it.copy(isUploadingCv = false) }
                return@launch
            }
            val fileName = resolveFileName(uri)
            profileRepository.uploadFile(userId, bytes, fileName, "application/pdf")
                .onSuccess { url ->
                    val updated = currentProfile.copy(userId = userId, cvUrl = url)
                    profileRepository.saveProfile(updated)
                        .onSuccess { saved ->
                            currentProfile = saved
                            _uiState.update { it.copy(isUploadingCv = false, cvUrl = url) }
                        }
                        .onFailure { _uiState.update { it.copy(isUploadingCv = false) } }
                }
                .onFailure { _uiState.update { it.copy(isUploadingCv = false) } }
        }
    }

    private fun resolveFileName(uri: Uri): String {
        var name = "curriculum.pdf"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && col >= 0) name = cursor.getString(col)
        }
        return name
    }

    @SuppressLint("MissingPermission")
    fun fetchAndSaveLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) {
                    val updated = currentProfile.copy(userId = userId, latitude = loc.latitude, longitude = loc.longitude)
                    profileRepository.saveProfile(updated).onSuccess { saved ->
                        currentProfile = saved
                        _uiState.update { it.copy(hasLocation = true) }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun onLogout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
