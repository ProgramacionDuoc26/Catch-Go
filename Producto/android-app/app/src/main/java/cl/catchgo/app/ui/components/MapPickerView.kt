package cl.catchgo.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal500
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val SANTIAGO = LatLng(-33.4489, -70.6693)

@Composable
fun MapPickerView(
    latitude: Double?,
    longitude: Double?,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val markerState = rememberMarkerState(position = SANTIAGO)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            if (latitude != null && longitude != null) LatLng(latitude, longitude) else SANTIAGO,
            if (latitude != null) 14f else 11f
        )
    }
    var hasMarker by remember { mutableStateOf(latitude != null && longitude != null) }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            val pos = LatLng(latitude, longitude)
            markerState.position = pos
            cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 14f)
            hasMarker = true
        }
    }

    Surface(shape = RoundedCornerShape(12.dp), color = Gray100, modifier = modifier) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Icon(Icons.Outlined.MyLocation, null, tint = Teal500, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        "Ubicación en mapa",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                    Text(
                        if (hasMarker) "Ubicación guardada · toca el mapa para actualizar"
                        else "Toca el mapa para fijar tu ubicación",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasMarker) Teal500 else Gray700
                    )
                }
            }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    markerState.position = latLng
                    hasMarker = true
                    onLocationSelected(latLng.latitude, latLng.longitude)
                }
            ) {
                if (hasMarker) {
                    Marker(state = markerState, draggable = true)
                }
            }
        }
    }
}
