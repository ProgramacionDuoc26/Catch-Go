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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import cl.catchgo.app.domain.model.HabilidadUsuario
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.model.UserRole
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.domain.model.ApplicationStatus
import cl.catchgo.app.domain.model.JobApplication
import cl.catchgo.app.ui.components.LevelBadge
import cl.catchgo.app.ui.components.MapPickerView
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.RadarChartView
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.components.StatusBadge
import cl.catchgo.app.ui.components.StatusType
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.Error50
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Gray900
import cl.catchgo.app.ui.theme.Navy
import cl.catchgo.app.ui.theme.NavyDeep
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal200
import cl.catchgo.app.ui.theme.Teal50
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.White
import coil.compose.AsyncImage
import java.io.File

private val SKILLS_HABILIDADES = listOf(
    "Atención al cliente", "Conducción", "Manejo Excel", "Inventario",
    "Ventas", "Programación", "Diseño", "Trabajo físico",
    "Electricidad", "Mecánica", "Liderazgo", "Logística"
)
private val SKILLS_AMBIENTE = listOf(
    "Trabajo en equipo", "Trabajo individual", "Trabajo en terreno",
    "Oficina", "Alta presión", "Flexible"
)
private val SKILLS_CARACTERISTICA = listOf(
    "Responsable", "Rápido", "Ordenado", "Creativo",
    "Líder", "Comunicativo", "Analítico"
)
private val SKILLS_PREFERENCIA = listOf("Part Time", "Turnos", "Freelance", "Temporal", "Fines de semana")

private val BANCOS = listOf(
    "Banco de Chile", "Banco Estado", "Banco Santander", "Banco BCI",
    "Scotiabank", "Banco Itaú", "Banco Bice", "Banco Security",
    "Banco Falabella", "Banco Ripley", "Banco Consorcio",
    "Banco Internacional", "Tenpo", "Mach"
)
private val TIPOS_CUENTA = listOf("VISTA" to "Cuenta Vista / RUT", "CORRIENTE" to "Cuenta Corriente", "AHORRO" to "Cuenta de Ahorro")

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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

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
    LaunchedEffect(session.user.id) {
        viewModel.loadProfile(session.user.id)
        if (userId > 0) viewModel.loadSkills(userId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) viewModel.clearSaveSuccess()
    }

    LaunchedEffect(uiState.accountDeleted) {
        if (uiState.accountDeleted) viewModel.onLogout()
    }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Foto de perfil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        showPhotoOptions = false
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            val uri = createCameraUri(context); cameraUri = uri; cameraLauncher.launch(uri)
                        } else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }, modifier = Modifier.fillMaxWidth()) { Text("Tomar foto") }
                    TextButton(onClick = { showPhotoOptions = false; galleryLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Elegir de galería") }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPhotoOptions = false }) { Text("Cancelar") } }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            password = deletePassword,
            onPasswordChange = { deletePassword = it },
            isDeleting = uiState.isDeletingAccount,
            error = uiState.deleteAccountError,
            onConfirm = { viewModel.deleteAccount(deletePassword) },
            onDismiss = { showDeleteDialog = false; deletePassword = ""; viewModel.clearDeleteError() }
        )
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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

            SectionCard(title = "Información Personal", icon = Icons.Outlined.Person) {
                ProfileField(
                    value = uiState.name,
                    onValueChange = { viewModel.onFieldChange("name", it) },
                    label = "Nombre completo",
                    icon = Icons.Outlined.Person
                )
                ProfileField(
                    value = uiState.rut,
                    onValueChange = { viewModel.onFieldChange("rut", it) },
                    label = "RUT",
                    placeholder = "12.345.678-9",
                    icon = Icons.Outlined.Badge
                )
                ProfileField(
                    value = uiState.description,
                    onValueChange = { viewModel.onFieldChange("description", it) },
                    label = "Sobre mí / Resumen profesional",
                    placeholder = "Cuéntanos sobre tu experiencia...",
                    singleLine = false,
                    minLines = 3
                )
                ProfileField(
                    value = uiState.phone,
                    onValueChange = { v ->
                        val fixed = if (!v.startsWith("+56 ")) "+56 " else {
                            "+56 " + v.drop(4).filter { it.isDigit() }.take(9)
                        }
                        viewModel.onFieldChange("phone", fixed)
                    },
                    label = "Teléfono",
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone
                )
                BirthDateField(
                    value = uiState.birthDate,
                    onChange = { viewModel.onFieldChange("birthDate", it) },
                    locked = uiState.birthDateLocked
                )
                ProfileField(
                    value = uiState.address,
                    onValueChange = { viewModel.onFieldChange("address", it) },
                    label = "Dirección",
                    placeholder = "Ej: Alameda 123, Santiago",
                    icon = Icons.Outlined.Home
                )
            }

            MapPickerView(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                onLocationSelected = { lat, lon -> viewModel.updateLocation(lat, lon) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Gray200)

            SkillsPreferenciasSection(
                selectedHabilidades = uiState.skillsHabilidades,
                onToggleHabilidad = { viewModel.toggleSkillsHabilidad(it) },
                ambiente = uiState.skillsAmbiente,
                onAmbienteChange = { viewModel.onFieldChange("skillsAmbiente", it) },
                caracteristica = uiState.skillsCaracteristica,
                onCaracteristicaChange = { viewModel.onFieldChange("skillsCaracteristica", it) },
                preferencia = uiState.skillsPreferencia,
                onPreferenciaChange = { viewModel.onFieldChange("skillsPreferencia", it) }
            )

            HorizontalDivider(color = Gray200)

            RadarSkillsSection(
                radar = uiState.radar,
                habilidades = uiState.habilidades,
                onManageSkills = onNavigateToSkills
            )

            HorizontalDivider(color = Gray200)

            HistorialSection(historial = uiState.historial)

            HorizontalDivider(color = Gray200)

            CvSection(
                cvUrl = uiState.cvUrl,
                isUploading = uiState.isUploadingCv,
                onPickCv = { cvLauncher.launch(arrayOf("application/pdf")) }
            )

            HorizontalDivider(color = Gray200)

            BankDataSection(
                bankName = uiState.bankName,
                onBankNameChange = { viewModel.onFieldChange("bankName", it) },
                accountType = uiState.accountType,
                onAccountTypeChange = { viewModel.onFieldChange("accountType", it) },
                accountNumber = uiState.accountNumber,
                onAccountNumberChange = { viewModel.onFieldChange("accountNumber", it) }
            )

            uiState.errorMessage?.let { msg ->
                Surface(color = Error50, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = msg,
                        color = Error600,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }

            PrimaryButton(
                text = if (uiState.isSaving) "Guardando..." else "Guardar Cambios",
                onClick = { viewModel.saveProfile() },
                loading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(
                text = "Cerrar sesión",
                onClick = viewModel::onLogout,
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = Error600)
            ) {
                Icon(Icons.Outlined.Warning, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar mi cuenta permanentemente", style = MaterialTheme.typography.labelMedium)
            }

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
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Box(modifier = Modifier.size(80.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Teal500.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isUploadingPhoto -> CircularProgressIndicator(color = White, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                        photoUrl != null -> AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else -> Text(text = session.user.fullName?.firstOrNull()?.uppercase() ?: "?", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = White)
                    }
                }
                if (!isUploadingPhoto) {
                    Box(
                        modifier = Modifier.size(26.dp).align(Alignment.BottomEnd).clip(CircleShape).background(White).clickable(onClick = onPickPhoto),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.CameraAlt, null, tint = Gray700, modifier = Modifier.size(14.dp)) }
                }
            }
            Spacer(Modifier.height(Spacing.md))
            Text(text = session.user.fullName ?: session.user.email, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = White)
            Text(text = session.user.email, style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.7f))
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Teal500.copy(alpha = 0.25f)).padding(horizontal = 12.dp, vertical = 5.dp)
                ) { Text("Trabajador", style = MaterialTheme.typography.labelMedium, color = Teal200) }
                LevelBadge(nivel = session.user.nivel)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Surface(color = White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Icon(icon, null, tint = Teal500, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Gray900)
            }
            HorizontalDivider(color = Gray200)
            content()
        }
    }
}

@Composable
private fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = Gray500) } },
        leadingIcon = icon?.let { { Icon(it, null, tint = Teal500, modifier = Modifier.size(18.dp)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun BirthDateField(value: String, onChange: (String) -> Unit, locked: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (!locked) onChange(it) },
        label = { Text(if (locked) "Fecha de nacimiento (bloqueada)" else "Fecha de nacimiento") },
        leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, tint = if (locked) Gray500 else Teal500, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (locked) ({ Icon(Icons.Outlined.Lock, null, tint = Gray500, modifier = Modifier.size(16.dp)) }) else null,
        enabled = !locked,
        placeholder = { Text("YYYY-MM-DD", color = Gray500) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        supportingText = if (locked) ({ Text("La fecha de nacimiento no se puede modificar", style = MaterialTheme.typography.labelSmall, color = Gray500) }) else null
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsPreferenciasSection(
    selectedHabilidades: List<String>,
    onToggleHabilidad: (String) -> Unit,
    ambiente: String,
    onAmbienteChange: (String) -> Unit,
    caracteristica: String,
    onCaracteristicaChange: (String) -> Unit,
    preferencia: String,
    onPreferenciaChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text("Habilidades y Preferencias", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Gray900)

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("1. ¿Cuáles son tus principales habilidades?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SKILLS_HABILIDADES.forEach { skill ->
                    val selected = selectedHabilidades.contains(skill)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal50 else Gray100,
                        modifier = Modifier.clickable { onToggleHabilidad(skill) }
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Teal500 else Gray700,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("2. ¿En qué ambiente trabajas mejor?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SKILLS_AMBIENTE.forEach { opt ->
                    val selected = ambiente == opt
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal500 else Gray100,
                        modifier = Modifier.clickable { onAmbienteChange(if (selected) "" else opt) }
                    ) {
                        Text(text = opt, style = MaterialTheme.typography.labelSmall, color = if (selected) White else Gray700, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("3. ¿Qué te caracteriza más?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SKILLS_CARACTERISTICA.forEach { opt ->
                    val selected = caracteristica == opt
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal500 else Gray100,
                        modifier = Modifier.clickable { onCaracteristicaChange(if (selected) "" else opt) }
                    ) {
                        Text(text = opt, style = MaterialTheme.typography.labelSmall, color = if (selected) White else Gray700, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("4. ¿Qué tipo de trabajo prefieres?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SKILLS_PREFERENCIA.forEach { opt ->
                    val selected = preferencia == opt
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal500 else Gray100,
                        modifier = Modifier.clickable { onPreferenciaChange(if (selected) "" else opt) }
                    ) {
                        Text(text = opt, style = MaterialTheme.typography.labelSmall, color = if (selected) White else Gray700, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RadarSkillsSection(
    radar: RadarData?,
    habilidades: List<HabilidadUsuario>,
    onManageSkills: () -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Habilidades registradas", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Gray900)
            TextButton(onClick = onManageSkills) { Text("Gestionar", color = Teal500, style = MaterialTheme.typography.labelMedium) }
        }
        if (radar != null && radar.ejes.any { it.valor > 0 }) {
            Spacer(Modifier.height(8.dp))
            RadarChartView(data = radar, modifier = Modifier.fillMaxWidth().height(260.dp))
        }
        if (habilidades.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                habilidades.forEach { h ->
                    Surface(shape = RoundedCornerShape(20.dp), color = Teal500.copy(alpha = 0.1f)) {
                        Text(text = h.nombre, style = MaterialTheme.typography.labelSmall, color = Teal500, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
        } else {
            Spacer(Modifier.height(4.dp))
            Text("Aún no has registrado habilidades.", style = MaterialTheme.typography.bodySmall, color = Gray500)
        }
    }
}

@Composable
private fun CvSection(cvUrl: String?, isUploading: Boolean, onPickCv: () -> Unit) {
    val context = LocalContext.current
    Column {
        Text("Currículum", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = Gray900)
        Spacer(Modifier.height(8.dp))
        when {
            isUploading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Teal500)
                Text("Subiendo...", style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
            cvUrl != null -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Description, null, tint = Teal500, modifier = Modifier.size(18.dp))
                    Text("CV cargado", style = MaterialTheme.typography.bodySmall, color = Gray700)
                }
                Row {
                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))) }) { Text("Ver", color = Teal500, style = MaterialTheme.typography.labelMedium) }
                    TextButton(onClick = onPickCv) { Text("Cambiar", color = Gray500, style = MaterialTheme.typography.labelMedium) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankDataSection(
    bankName: String,
    onBankNameChange: (String) -> Unit,
    accountType: String,
    onAccountTypeChange: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit
) {
    SectionCard(title = "Datos de Transferencia", icon = Icons.Outlined.AccountBalance) {
        var bancosExpanded by remember { mutableStateOf(false) }
        var tipoExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded = bancosExpanded, onExpandedChange = { bancosExpanded = it }) {
            OutlinedTextField(
                value = bankName.ifBlank { "Seleccionar banco..." },
                onValueChange = {},
                readOnly = true,
                label = { Text("Banco") },
                leadingIcon = { Icon(Icons.Outlined.AccountBalance, null, tint = Teal500, modifier = Modifier.size(18.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bancosExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(expanded = bancosExpanded, onDismissRequest = { bancosExpanded = false }) {
                BANCOS.forEach { banco ->
                    DropdownMenuItem(text = { Text(banco) }, onClick = { onBankNameChange(banco); bancosExpanded = false })
                }
            }
        }

        ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = it }) {
            val tipoLabel = TIPOS_CUENTA.find { it.first == accountType }?.second ?: "Seleccionar tipo..."
            OutlinedTextField(
                value = tipoLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de cuenta") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                TIPOS_CUENTA.forEach { (value, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { onAccountTypeChange(value); tipoExpanded = false })
                }
            }
        }

        ProfileField(
            value = accountNumber,
            onValueChange = onAccountNumberChange,
            label = "Número de cuenta",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    isDeleting: Boolean,
    error: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Warning, null, tint = Error600, modifier = Modifier.size(28.dp)) },
        title = { Text("¿Eliminar cuenta?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "Esta acción es permanente y no se puede deshacer. Se eliminarán todos tus datos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    isError = error != null
                )
                if (error != null) {
                    Text(error, style = MaterialTheme.typography.labelSmall, color = Error600)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.textButtonColors(contentColor = Error600)
            ) {
                if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Error600)
                else Text("Eliminar cuenta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun HistorialSection(historial: List<JobApplication>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            "Historial de Postulaciones",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Gray900
        )
        if (historial.isEmpty()) {
            Text("Sin postulaciones recientes.", style = MaterialTheme.typography.bodySmall, color = Gray500)
        } else {
            historial.forEach { app -> HistorialItem(app) }
        }
    }
}

@Composable
private fun HistorialItem(app: JobApplication) {
    Surface(
        color = White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    app.offerTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Gray900,
                    maxLines = 1
                )
                if (app.company.isNotBlank()) {
                    Text(app.company, style = MaterialTheme.typography.bodySmall, color = Gray500, maxLines = 1)
                }
                if (app.createdAtIso.isNotBlank()) {
                    Text(
                        app.createdAtIso.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }
            }
            Spacer(Modifier.width(Spacing.sm))
            val (badgeText, badgeType) = when (app.status) {
                ApplicationStatus.ACCEPTED -> "Aceptado" to StatusType.Success
                ApplicationStatus.REJECTED -> "Rechazado" to StatusType.Error
                else -> "Pendiente" to StatusType.Pending
            }
            StatusBadge(text = badgeText, type = badgeType)
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
