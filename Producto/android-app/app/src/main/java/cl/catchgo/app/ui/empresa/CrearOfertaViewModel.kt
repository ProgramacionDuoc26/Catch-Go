package cl.catchgo.app.ui.empresa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.catchgo.app.data.local.SessionStore
import cl.catchgo.app.data.remote.JobsApi
import cl.catchgo.app.data.remote.dto.CreateJobOfferRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrearOfertaUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val ubicacion: String = "Santiago, RM",
    val categoria: String = "Guardia",
    val remuneracion: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@HiltViewModel
class CrearOfertaViewModel @Inject constructor(
    private val jobsApi: JobsApi,
    private val sessionStore: SessionStore
) : ViewModel() {

    val state: StateFlow<CrearOfertaUiState> get() = _state
    private val _state = MutableStateFlow(CrearOfertaUiState())

    fun onTituloChange(v: String) = _state.update { it.copy(titulo = v) }
    fun onDescripcionChange(v: String) = _state.update { it.copy(descripcion = v) }
    fun onUbicacionChange(v: String) = _state.update { it.copy(ubicacion = v) }
    fun onCategoriaChange(v: String) = _state.update { it.copy(categoria = v) }
    fun onRemuneracionChange(v: String) = _state.update { it.copy(remuneracion = v) }
    fun onFechaInicioChange(v: String) = _state.update { it.copy(fechaInicio = v) }
    fun onFechaFinChange(v: String) = _state.update { it.copy(fechaFin = v) }
    fun onLocationChange(lat: Double, lon: Double) = _state.update { it.copy(latitude = lat, longitude = lon) }

    fun searchAddress(context: android.content.Context, addressQuery: String) {
        if (addressQuery.isBlank()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(addressQuery, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    _state.update { it.copy(latitude = addr.latitude, longitude = addr.longitude, error = null) }
                } else {
                    _state.update { it.copy(error = "No se pudo encontrar la dirección") }
                }
            }.onFailure { e ->
                _state.update { it.copy(error = "Error al buscar dirección: ${e.localizedMessage}") }
            }
        }
    }

    fun reverseGeocode(context: android.content.Context, lat: Double, lon: Double) {
        _state.update { it.copy(latitude = lat, longitude = lon) }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val fullAddress = addr.getAddressLine(0) ?: "${addr.thoroughfare ?: ""} ${addr.subThoroughfare ?: ""}, ${addr.locality ?: ""}".trim()
                    if (fullAddress.isNotBlank()) {
                        _state.update { it.copy(ubicacion = fullAddress) }
                    }
                }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (s.titulo.isBlank() || s.descripcion.isBlank() || s.remuneracion.isBlank() || s.fechaInicio.isBlank()) {
            _state.update { it.copy(error = "Completa todos los campos obligatorios") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val session = sessionStore.session.first() ?: return@launch
                val request = CreateJobOfferRequest(
                    titulo = s.titulo,
                    descripcion = s.descripcion,
                    ubicacion = s.ubicacion,
                    categoria = s.categoria,
                    remuneracion = s.remuneracion.toIntOrNull() ?: 0,
                    fechaInicio = s.fechaInicio,
                    fechaFin = s.fechaFin.ifBlank { null },
                    empresaId = session.user.id,
                    latitude = s.latitude,
                    longitude = s.longitude
                )
                jobsApi.create(request)
                _state.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
