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
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.domain.repository.ApplicationsRepository
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
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val radar: RadarData? = null,
    val habilidades: List<HabilidadUsuario> = emptyList(),
    val photoUrl: String? = null,
    val cvUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val isUploadingCv: Boolean = false,
    val hasLocation: Boolean = false,
    val name: String = "",
    val rut: String = "",
    val phone: String = "+56 ",
    val birthDate: String = "",
    val address: String = "",
    val description: String = "",
    val bankName: String = "",
    val accountType: String = "",
    val accountNumber: String = "",
    val birthDateLocked: Boolean = false,
    val skillsHabilidades: List<String> = emptyList(),
    val skillsAmbiente: String = "",
    val skillsCaracteristica: String = "",
    val skillsPreferencia: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val historial: List<JobApplication> = emptyList(),
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null,
    val accountDeleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val habilidadesRepository: HabilidadesRepository,
    private val profileRepository: ProfileRepository,
    private val applicationsRepository: ApplicationsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            applicationsRepository.observeMyApplications().collect { apps ->
                _uiState.update { it.copy(historial = apps.take(3)) }
            }
        }
    }

    private var userId: String = ""
    private var currentProfile: ProfileRemoteDto = ProfileRemoteDto()

    fun loadProfile(userId: String) {
        this.userId = userId
        viewModelScope.launch { applicationsRepository.refreshFromBackend() }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            profileRepository.getProfile(userId).onSuccess { profile ->
                if (profile != null) {
                    currentProfile = profile
                    val skillsJson = runCatching {
                        if (profile.skills?.startsWith("{") == true) JSONObject(profile.skills) else null
                    }.getOrNull()

                    val habilidades = runCatching {
                        val arr = skillsJson?.optJSONArray("habilidades") ?: JSONArray()
                        (0 until arr.length()).map { arr.getString(it) }
                    }.getOrDefault(emptyList())

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            photoUrl = profile.photoUrl,
                            cvUrl = profile.cvUrl,
                            latitude = profile.latitude,
                            longitude = profile.longitude,
                            hasLocation = profile.latitude != null && profile.longitude != null,
                            name = profile.name ?: "",
                            rut = profile.rut ?: skillsJson?.optString("rut", "") ?: "",
                            phone = profile.phone?.takeIf { p -> p.isNotBlank() } ?: "+56 ",
                            birthDate = profile.birthDate ?: "",
                            address = skillsJson?.optString("address", "") ?: "",
                            description = profile.description ?: "",
                            bankName = profile.bankName ?: "",
                            accountType = profile.accountType ?: "",
                            accountNumber = profile.accountNumber ?: "",
                            birthDateLocked = profile.birthDate?.isNotBlank() == true,
                            skillsHabilidades = habilidades,
                            skillsAmbiente = skillsJson?.optString("ambiente", "") ?: "",
                            skillsCaracteristica = skillsJson?.optString("caracteristica", "") ?: "",
                            skillsPreferencia = skillsJson?.optString("preferencia", "") ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFieldChange(field: String, value: String) {
        _uiState.update { s ->
            when (field) {
                "name" -> s.copy(name = value)
                "rut" -> s.copy(rut = value)
                "phone" -> s.copy(phone = value)
                "birthDate" -> s.copy(birthDate = value)
                "address" -> s.copy(address = value)
                "description" -> s.copy(description = value)
                "bankName" -> s.copy(bankName = value)
                "accountType" -> s.copy(accountType = value)
                "accountNumber" -> s.copy(accountNumber = value)
                "skillsAmbiente" -> s.copy(skillsAmbiente = value)
                "skillsCaracteristica" -> s.copy(skillsCaracteristica = value)
                "skillsPreferencia" -> s.copy(skillsPreferencia = value)
                else -> s
            }
        }
    }

    fun toggleSkillsHabilidad(habilidad: String) {
        _uiState.update { s ->
            val updated = if (s.skillsHabilidades.contains(habilidad))
                s.skillsHabilidades - habilidad
            else
                s.skillsHabilidades + habilidad
            s.copy(skillsHabilidades = updated)
        }
    }

    fun saveProfile() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val existingSkills = runCatching {
                if (currentProfile.skills?.startsWith("{") == true)
                    JSONObject(currentProfile.skills) else JSONObject()
            }.getOrDefault(JSONObject())

            existingSkills.put("address", s.address)
            existingSkills.put("rut", s.rut)

            val habArr = JSONArray()
            s.skillsHabilidades.forEach { habArr.put(it) }
            existingSkills.put("habilidades", habArr)
            existingSkills.put("ambiente", s.skillsAmbiente)
            existingSkills.put("caracteristica", s.skillsCaracteristica)
            existingSkills.put("preferencia", s.skillsPreferencia)

            val dto = currentProfile.copy(
                userId = userId,
                name = s.name,
                rut = s.rut,
                phone = s.phone,
                birthDate = s.birthDate.takeIf { it.isNotBlank() },
                description = s.description,
                bankName = s.bankName,
                accountType = s.accountType,
                accountNumber = s.accountNumber,
                skills = existingSkills.toString()
            )
            profileRepository.saveProfile(dto)
                .onSuccess { saved ->
                    currentProfile = saved
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            birthDateLocked = saved.birthDate?.isNotBlank() == true
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Error al guardar") }
                }
        }
    }

    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun clearDeleteError() = _uiState.update { it.copy(deleteAccountError = null) }

    fun loadSkills(userId: Long) {
        viewModelScope.launch {
            val radar = habilidadesRepository.getRadar(userId).getOrNull()
            val skills = habilidadesRepository.getHabilidadesUsuario(userId).getOrDefault(emptyList())
            _uiState.update { it.copy(radar = radar, habilidades = skills) }
        }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: run {
                _uiState.update { it.copy(isUploadingPhoto = false) }; return@launch
            }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png")) ".png" else ".jpg"
            profileRepository.uploadFile(userId, bytes, "photo$ext", mimeType)
                .onSuccess { url ->
                    val updated = currentProfile.copy(userId = userId, photoUrl = url)
                    profileRepository.saveProfile(updated).onSuccess { saved ->
                        currentProfile = saved
                        _uiState.update { it.copy(isUploadingPhoto = false, photoUrl = url) }
                    }.onFailure { _uiState.update { it.copy(isUploadingPhoto = false) } }
                }
                .onFailure { _uiState.update { it.copy(isUploadingPhoto = false) } }
        }
    }

    fun onCvPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCv = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: run {
                _uiState.update { it.copy(isUploadingCv = false) }; return@launch
            }
            val fileName = resolveFileName(uri)
            profileRepository.uploadFile(userId, bytes, fileName, "application/pdf")
                .onSuccess { url ->
                    val updated = currentProfile.copy(userId = userId, cvUrl = url)
                    profileRepository.saveProfile(updated).onSuccess { saved ->
                        currentProfile = saved
                        _uiState.update { it.copy(isUploadingCv = false, cvUrl = url) }
                    }.onFailure { _uiState.update { it.copy(isUploadingCv = false) } }
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

    fun updateLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(latitude = lat, longitude = lon, hasLocation = true) }
            val s = _uiState.value
            val existingSkills = runCatching {
                if (currentProfile.skills?.startsWith("{") == true)
                    org.json.JSONObject(currentProfile.skills) else org.json.JSONObject()
            }.getOrDefault(org.json.JSONObject())
            existingSkills.put("address", s.address)
            existingSkills.put("rut", s.rut)
            val habArr = org.json.JSONArray()
            s.skillsHabilidades.forEach { habArr.put(it) }
            existingSkills.put("habilidades", habArr)
            existingSkills.put("ambiente", s.skillsAmbiente)
            existingSkills.put("caracteristica", s.skillsCaracteristica)
            existingSkills.put("preferencia", s.skillsPreferencia)
            val updated = currentProfile.copy(
                userId = userId, latitude = lat, longitude = lon,
                skills = existingSkills.toString()
            )
            profileRepository.saveProfile(updated).onSuccess { saved -> currentProfile = saved }
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deleteAccountError = null) }
            authRepository.verifyPassword(userId, password)
                .onSuccess { valid ->
                    if (!valid) {
                        _uiState.update { it.copy(isDeletingAccount = false, deleteAccountError = "Contraseña incorrecta") }
                        return@launch
                    }
                    profileRepository.deleteProfile(userId)
                    authRepository.deleteAccount(userId)
                        .onSuccess { _uiState.update { it.copy(isDeletingAccount = false, accountDeleted = true) } }
                        .onFailure { e -> _uiState.update { it.copy(isDeletingAccount = false, deleteAccountError = e.message ?: "Error al eliminar cuenta") } }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isDeletingAccount = false, deleteAccountError = e.message ?: "Error al verificar contraseña") }
                }
        }
    }

    fun onLogout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
