package cl.catchgo.app.ui.empresa

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.remote.dto.ProfileRemoteDto
import cl.catchgo.app.domain.model.EjeRadar
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.repository.AuthRepository
import cl.catchgo.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class EmpresaPerfilUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val photoUrl: String? = null,
    val docUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val isUploadingDoc: Boolean = false,
    val hasLocation: Boolean = false,
    val name: String = "",
    val rut: String = "",
    val representativeName: String = "",
    val email: String = "",
    val phone: String = "+56 ",
    val address: String = "",
    val description: String = "",
    val bankName: String = "",
    val accountType: String = "",
    val accountNumber: String = "",
    val giro: String = "",
    val tipoTrabajador: String = "",
    val habilidadValorada: String = "",
    val ritmo: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radarData: RadarData? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null,
    val accountDeleted: Boolean = false,
    val plan: String = "FREE",
    val nameLocked: Boolean = false,
    val rutLocked: Boolean = false,
    val representativeNameLocked: Boolean = false,
    val culturaLocked: Boolean = false
)

@HiltViewModel
class EmpresaPerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmpresaPerfilUiState())
    val uiState: StateFlow<EmpresaPerfilUiState> = _uiState.asStateFlow()

    private var userId: String = ""
    private var currentProfile: ProfileRemoteDto = ProfileRemoteDto()

    fun loadProfile(userId: String) {
        this.userId = userId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            profileRepository.getProfile(userId).onSuccess { profile ->
                if (profile != null) {
                    currentProfile = profile
                    val skills = runCatching {
                        if (profile.skills?.startsWith("{") == true) JSONObject(profile.skills) else null
                    }.getOrNull()

                    val plan = profile.plan ?: "FREE"
                    val isPremium = plan == "PREMIUM" || plan == "ENTERPRISE"
                    val giroSaved = skills?.optString("giro")?.isNotBlank() == true
                    val tipoSaved = skills?.optString("tipoTrabajador")?.isNotBlank() == true
                    val culturaLocked = (giroSaved || tipoSaved) && !isPremium

                    val newState = _uiState.value.copy(
                        isLoading = false,
                        photoUrl = profile.photoUrl,
                        docUrl = profile.cvUrl,
                        hasLocation = profile.latitude != null && profile.longitude != null,
                        latitude = profile.latitude,
                        longitude = profile.longitude,
                        name = profile.name ?: "",
                        rut = profile.rut ?: skills?.optString("rut", "") ?: "",
                        representativeName = skills?.optString("representativeName", "") ?: "",
                        email = profile.email ?: "",
                        phone = profile.phone?.takeIf { p -> p.isNotBlank() } ?: "+56 ",
                        address = skills?.optString("address", "") ?: "",
                        description = profile.description ?: "",
                        bankName = profile.bankName ?: "",
                        accountType = profile.accountType ?: "",
                        accountNumber = profile.accountNumber ?: "",
                        giro = skills?.optString("giro", "") ?: "",
                        tipoTrabajador = skills?.optString("tipoTrabajador", "") ?: "",
                        habilidadValorada = skills?.optString("habilidadValorada", "") ?: "",
                        ritmo = skills?.optString("ritmo", "") ?: "",
                        plan = plan,
                        nameLocked = !profile.name.isNullOrBlank(),
                        rutLocked = !profile.rut.isNullOrBlank() || !skills?.optString("rut").isNullOrBlank(),
                        representativeNameLocked = !skills?.optString("representativeName").isNullOrBlank(),
                        culturaLocked = culturaLocked
                    )
                    _uiState.update { newState.copy(radarData = computeRadar(newState)) }
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
            val updated = when (field) {
                "name" -> s.copy(name = value)
                "rut" -> s.copy(rut = value)
                "representativeName" -> s.copy(representativeName = value)
                "phone" -> s.copy(phone = value)
                "address" -> s.copy(address = value)
                "description" -> s.copy(description = value)
                "bankName" -> s.copy(bankName = value)
                "accountType" -> s.copy(accountType = value)
                "accountNumber" -> s.copy(accountNumber = value)
                "giro" -> s.copy(giro = value)
                "tipoTrabajador" -> s.copy(tipoTrabajador = value)
                "habilidadValorada" -> s.copy(habilidadValorada = value)
                "ritmo" -> s.copy(ritmo = value)
                else -> s
            }
            updated.copy(radarData = computeRadar(updated))
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

            existingSkills.put("rut", s.rut)
            existingSkills.put("representativeName", s.representativeName)
            existingSkills.put("address", s.address)
            existingSkills.put("giro", s.giro)
            existingSkills.put("tipoTrabajador", s.tipoTrabajador)
            existingSkills.put("habilidadValorada", s.habilidadValorada)
            existingSkills.put("ritmo", s.ritmo)

            val dto = currentProfile.copy(
                userId = userId,
                name = s.name,
                rut = s.rut,
                phone = s.phone,
                description = s.description,
                bankName = s.bankName,
                accountType = s.accountType,
                accountNumber = s.accountNumber,
                skills = existingSkills.toString()
            )
            profileRepository.saveProfile(dto)
                .onSuccess { saved ->
                    currentProfile = saved
                    val plan = saved.plan ?: "FREE"
                    val isPremium = plan == "PREMIUM" || plan == "ENTERPRISE"
                    val giroSaved = s.giro.isNotBlank()
                    val tipoSaved = s.tipoTrabajador.isNotBlank()
                    val culturaLocked = (giroSaved || tipoSaved) && !isPremium

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            plan = plan,
                            nameLocked = !saved.name.isNullOrBlank(),
                            rutLocked = !saved.rut.isNullOrBlank() || s.rut.isNotBlank(),
                            representativeNameLocked = s.representativeName.isNotBlank(),
                            culturaLocked = culturaLocked
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

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: run {
                _uiState.update { it.copy(isUploadingPhoto = false) }; return@launch
            }
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("png")) ".png" else ".jpg"
            profileRepository.uploadFile(userId, bytes, "logo$ext", mimeType)
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

    fun onDocPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingDoc = true) }
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: run {
                _uiState.update { it.copy(isUploadingDoc = false) }; return@launch
            }
            val fileName = resolveFileName(uri)
            val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"
            profileRepository.uploadFile(userId, bytes, fileName, mimeType)
                .onSuccess { url ->
                    val updated = currentProfile.copy(userId = userId, cvUrl = url)
                    profileRepository.saveProfile(updated).onSuccess { saved ->
                        currentProfile = saved
                        _uiState.update { it.copy(isUploadingDoc = false, docUrl = url) }
                    }.onFailure { _uiState.update { it.copy(isUploadingDoc = false) } }
                }
                .onFailure { _uiState.update { it.copy(isUploadingDoc = false) } }
        }
    }

    private fun resolveFileName(uri: Uri): String {
        var name = "documento.pdf"
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
            val updated = currentProfile.copy(userId = userId, latitude = lat, longitude = lon)
            profileRepository.saveProfile(updated).onSuccess { saved -> currentProfile = saved }
        }
    }

    fun searchAddress(addressQuery: String) {
        if (addressQuery.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(addressQuery, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    _uiState.update { it.copy(latitude = addr.latitude, longitude = addr.longitude, hasLocation = true, errorMessage = null) }
                    val updated = currentProfile.copy(userId = userId, latitude = addr.latitude, longitude = addr.longitude)
                    profileRepository.saveProfile(updated).onSuccess { saved -> currentProfile = saved }
                } else {
                    _uiState.update { it.copy(errorMessage = "No se pudo encontrar la dirección") }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Error al buscar dirección: ${e.localizedMessage}") }
            }
        }
    }

    fun reverseGeocode(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lon, hasLocation = true) }
        val updated = currentProfile.copy(userId = userId, latitude = lat, longitude = lon)
        viewModelScope.launch {
            profileRepository.saveProfile(updated).onSuccess { saved -> currentProfile = saved }
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val fullAddress = addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}, ${addr.locality ?: ""}".trim()
                    if (fullAddress.isNotBlank()) {
                        _uiState.update { it.copy(address = fullAddress) }
                    }
                }
            }
        }
    }

    private fun computeRadar(s: EmpresaPerfilUiState): RadarData {
        val perfilScore = listOf(s.name, s.rut, s.representativeName, s.phone, s.address, s.description)
            .count { it.isNotBlank() }.toDouble() / 6.0 * 100.0
        return RadarData(listOf(
            EjeRadar(1, "Perfil", perfilScore),
            EjeRadar(2, "Giro", if (s.giro.isNotBlank()) 100.0 else 0.0),
            EjeRadar(3, "Tipo", if (s.tipoTrabajador.isNotBlank()) 100.0 else 0.0),
            EjeRadar(4, "Habilidad", if (s.habilidadValorada.isNotBlank()) 100.0 else 0.0),
            EjeRadar(5, "Ritmo", if (s.ritmo.isNotBlank()) 100.0 else 0.0)
        ))
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
