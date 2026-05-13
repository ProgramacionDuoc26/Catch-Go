package cl.catchgo.app.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.components.LevelBadge
import cl.catchgo.app.ui.components.RadarChartView
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.NavyDeep
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal200
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.White
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ProfilePlaceholderScreen(
    session: UserSession,
    onNavigateToSkills: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = session.user.id.toLongOrNull() ?: 0L
    val context = LocalContext.current

    var showPhotoOptions by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onPhotoPicked(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { viewModel.onPhotoPicked(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val cvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onCvPicked(it) }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.fetchAndSaveLocation()
    }

    LaunchedEffect(session.user.id) {
        viewModel.loadProfile(session.user.id)
        if (userId > 0 && session.user.role == UserRole.WORKER) {
            viewModel.loadSkills(userId)
        }
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Foto de perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasCam) {
                                val uri = createCameraUri(context)
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Tomar foto") }
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Elegir de galería") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptions = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            session = session,
            photoUrl = uiState.photoUrl,
            isUploadingPhoto = uiState.isUploadingPhoto,
            onPickPhoto = { showPhotoOptions = true }
        )

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            InfoSection(
                session = session,
                hasLocation = uiState.hasLocation,
                onSetLocation = {
                    val fineOk = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fineOk) {
                        viewModel.fetchAndSaveLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                }
            )

            if (session.user.role == UserRole.WORKER) {
                HorizontalDivider(color = Gray200)
                SkillsSection(
                    radar = uiState.radar,
                    habilidades = uiState.habilidades,
                    onManageSkills = onNavigateToSkills
                )
                HorizontalDivider(color = Gray200)
                CvSection(
                    cvUrl = uiState.cvUrl,
                    isUploading = uiState.isUploadingCv,
                    onPickCv = { cvLauncher.launch(arrayOf("application/pdf")) }
                )
            }

            HorizontalDivider(color = Gray200)

            Spacer(Modifier.height(Spacing.sm))

            SecondaryButton(
                text = "Cerrar sesión",
                onClick = viewModel::onLogout,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ProfileHeader(
    session: UserSession,
    photoUrl: String?,
    isUploadingPhoto: Boolean,
    onPickPhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(NavyDeep, Navy, BrandBlue600)))
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(modifier = Modifier.size(80.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Teal500.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploadingPhoto -> CircularProgressIndicator(
                            color = White,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                        photoUrl != null -> AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        else -> Text(
                            text = session.user.fullName?.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White
                        )
                    }
                }

                if (!isUploadingPhoto) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(White)
                            .clickable(onClick = onPickPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = Gray700,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = session.user.fullName ?: session.user.email,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = White
            )
            Text(
                text = session.user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(Spacing.sm))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Teal500.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = roleLabel(session.user.role),
                        style = MaterialTheme.typography.labelMedium,
                        color = Teal200
                    )
                }

                if (session.user.role == UserRole.WORKER) {
                    LevelBadge(nivel = session.user.nivel)
                }
            }
        }
    }
}

@Composable
private fun InfoSection(
    session: UserSession,
    hasLocation: Boolean,
    onSetLocation: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Gray100,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Información de cuenta",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900
            )
            Spacer(Modifier.height(Spacing.sm))
            InfoRow(icon = Icons.Outlined.AlternateEmail, label = "Email", value = session.user.email)
            if (session.user.fullName != null) {
                InfoRow(icon = Icons.Outlined.Badge, label = "Nombre", value = session.user.fullName)
            }
            InfoRow(icon = Icons.Outlined.WorkOutline, label = "Rol", value = roleLabel(session.user.role))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.MyLocation, contentDescription = null, tint = Teal500, modifier = Modifier.size(18.dp))
                    Column {
                        Text(text = "Ubicación", style = MaterialTheme.typography.labelSmall, color = Gray500)
                        Text(
                            text = if (hasLocation) "Guardada" else "No establecida",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray900
                        )
                    }
                }
                TextButton(onClick = onSetLocation) {
                    Text(
                        text = if (hasLocation) "Actualizar" else "Establecer",
                        color = Teal500,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsSection(
    radar: RadarData?,
    habilidades: List<HabilidadUsuario>,
    onManageSkills: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habilidades",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900
            )
            TextButton(onClick = onManageSkills) {
                Text("Gestionar", color = Teal500, style = MaterialTheme.typography.labelMedium)
            }
        }

        if (radar != null && radar.ejes.any { it.valor > 0 }) {
            Spacer(Modifier.height(8.dp))
            RadarChartView(
                data = radar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

        if (habilidades.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                habilidades.forEach { h ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Teal500.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = h.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = Teal500,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Aún no has agregado habilidades.",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun CvSection(
    cvUrl: String?,
    isUploading: Boolean,
    onPickCv: () -> Unit
) {
    val context = LocalContext.current

    Column {
        Text(
            text = "Currículum",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Gray900
        )
        Spacer(Modifier.height(8.dp))

        when {
            isUploading -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Teal500)
                Text("Subiendo...", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }

            cvUrl != null -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Description, null, tint = Teal500, modifier = Modifier.size(18.dp))
                    Text("CV cargado", style = MaterialTheme.typography.bodySmall, color = Gray700)
                }
                Row {
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl)))
                    }) {
                        Text("Ver", color = Teal500, style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = onPickCv) {
                        Text("Cambiar", color = Gray500, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            else -> OutlinedButton(
                onClick = onPickCv,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
            ) {
                Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Subir CV (PDF)", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Teal500, modifier = Modifier.size(18.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Gray900)
        }
    }
}

private fun roleLabel(role: UserRole) = when (role) {
    UserRole.WORKER -> "Trabajador"
    UserRole.EMPRESA -> "Empresa"
    UserRole.UNKNOWN -> "Sin rol"
}

private fun createCameraUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "images").also { it.mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
