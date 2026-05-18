package cl.catchgo.app.ui.empresa

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
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import cl.catchgo.app.domain.model.RadarData
import cl.catchgo.app.domain.model.UserSession
import cl.catchgo.app.ui.components.MapPickerView
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.RadarChartView
import cl.catchgo.app.ui.components.SecondaryButton
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

private val GIROS = listOf("Logística", "Retail", "Construcción", "Tecnología", "Salud", "Gastronomía", "Industrial", "Seguridad", "Minería", "Servicios")
private val TIPOS_TRABAJADOR = listOf("Operativo", "Técnico", "Administrativo", "Comercial", "TI / Informática", "Supervisor", "Multifuncional")
private val HABILIDADES_VALORADAS = listOf("Responsabilidad", "Puntualidad", "Liderazgo", "Adaptabilidad", "Trabajo en equipo", "Rapidez", "Comunicación", "Resolución de problemas")
private val RITMOS = listOf("Muy dinámico", "Moderado", "Estructurado", "Alta presión", "Flexible")
private val BANCOS = listOf(
    "Banco de Chile", "Banco Estado", "Banco Santander", "Banco BCI",
    "Scotiabank", "Banco Itaú", "Banco Bice", "Banco Security",
    "Banco Falabella", "Banco Ripley", "Banco Consorcio",
    "Banco Internacional", "Tenpo", "Mach"
)
private val TIPOS_CUENTA = listOf("VISTA" to "Cuenta Vista / RUT", "CORRIENTE" to "Cuenta Corriente", "AHORRO" to "Cuenta de Ahorro")

@Composable
fun EmpresaPerfilScreen(
    session: UserSession,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmpresaPerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
            val uri = createLogoUri(context); cameraUri = uri; cameraLauncher.launch(uri)
        }
    }
    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onDocPicked(it) }
    }
    LaunchedEffect(session.user.id) { viewModel.loadProfile(session.user.id) }
    LaunchedEffect(uiState.saveSuccess) { if (uiState.saveSuccess) viewModel.clearSaveSuccess() }
    LaunchedEffect(uiState.accountDeleted) { if (uiState.accountDeleted) viewModel.onLogout() }

    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Logo de empresa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        showPhotoOptions = false
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            val uri = createLogoUri(context); cameraUri = uri; cameraLauncher.launch(uri)
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
        EmpresaDeleteDialog(
            password = deletePassword,
            onPasswordChange = { deletePassword = it },
            isDeleting = uiState.isDeletingAccount,
            error = uiState.deleteAccountError,
            onConfirm = { viewModel.deleteAccount(deletePassword) },
            onDismiss = { showDeleteDialog = false; deletePassword = ""; viewModel.clearDeleteError() }
        )
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        EmpresaHeader(
            session = session,
            photoUrl = uiState.photoUrl,
            isUploading = uiState.isUploadingPhoto,
            onPickPhoto = { showPhotoOptions = true },
            onNavigateToSettings = onNavigateToSettings
        )

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.sm))

            EmpresaSectionCard(title = "Información de la Organización", icon = Icons.Outlined.Business) {
                EmpresaField(uiState.name, { viewModel.onFieldChange("name", it) }, "Nombre de la empresa", placeholder = "Nombre legal o fantasía", icon = Icons.Outlined.Business)
                EmpresaField(uiState.rut, { viewModel.onFieldChange("rut", it) }, "RUT de la empresa", placeholder = "76.123.456-K")
                EmpresaField(uiState.representativeName, { viewModel.onFieldChange("representativeName", it) }, "Representante Legal", placeholder = "Juan Pérez", icon = Icons.Outlined.Person)
                EmpresaField(uiState.phone, { v ->
                    val fixed = if (!v.startsWith("+56 ")) "+56 " else "+56 " + v.drop(4).filter { it.isDigit() }.take(9)
                    viewModel.onFieldChange("phone", fixed)
                }, "Teléfono corporativo", icon = Icons.Outlined.Phone, keyboardType = KeyboardType.Phone)
                EmpresaField(uiState.address, { viewModel.onFieldChange("address", it) }, "Dirección de la empresa", placeholder = "Calle Falsa 123, Santiago", icon = Icons.Outlined.Home)
                EmpresaField(
                    value = uiState.description,
                    onValueChange = { viewModel.onFieldChange("description", it) },
                    label = "Descripción de la empresa",
                    placeholder = "Describe la misión y visión...",
                    singleLine = false,
                    minLines = 3
                )
            }

            MapPickerView(
                latitude = uiState.latitude,
                longitude = uiState.longitude,
                onLocationSelected = { lat, lon -> viewModel.updateLocation(lat, lon) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Gray200)

            CulturaSection(
                giro = uiState.giro,
                onGiroChange = { viewModel.onFieldChange("giro", it) },
                tipoTrabajador = uiState.tipoTrabajador,
                onTipoChange = { viewModel.onFieldChange("tipoTrabajador", it) },
                habilidadValorada = uiState.habilidadValorada,
                onHabilidadChange = { viewModel.onFieldChange("habilidadValorada", it) },
                ritmo = uiState.ritmo,
                onRitmoChange = { viewModel.onFieldChange("ritmo", it) }
            )

            HorizontalDivider(color = Gray200)

            AdnCorporativoSection(radarData = uiState.radarData)

            HorizontalDivider(color = Gray200)

            DocLegalSection(
                docUrl = uiState.docUrl,
                isUploading = uiState.isUploadingDoc,
                onPickDoc = { docLauncher.launch(arrayOf("application/pdf", "image/*")) }
            )

            HorizontalDivider(color = Gray200)

            EmpresaBankSection(
                bankName = uiState.bankName,
                onBankChange = { viewModel.onFieldChange("bankName", it) },
                accountType = uiState.accountType,
                onAccountTypeChange = { viewModel.onFieldChange("accountType", it) },
                accountNumber = uiState.accountNumber,
                onAccountNumberChange = { viewModel.onFieldChange("accountNumber", it) }
            )

            uiState.errorMessage?.let { msg ->
                Surface(color = Error50, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(msg, color = Error600, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(Spacing.md))
                }
            }

            PrimaryButton(
                text = if (uiState.isSaving) "Guardando..." else "Guardar Perfil Corporativo",
                onClick = { viewModel.saveProfile() },
                loading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            SecondaryButton(text = "Cerrar sesión", onClick = viewModel::onLogout, modifier = Modifier.fillMaxWidth())

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = Error600)
            ) {
                Icon(Icons.Outlined.Warning, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Eliminar cuenta de empresa", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun EmpresaHeader(
    session: UserSession,
    photoUrl: String?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(colors = listOf(NavyDeep, Navy, BrandBlue600)))
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl)
    ) {
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Ajustes",
                tint = White
            )
        }

        Column(horizontalAlignment = Alignment.Start) {
            Box(modifier = Modifier.size(80.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    color = White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            isUploading -> CircularProgressIndicator(color = White, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                            photoUrl != null -> AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            else -> Icon(Icons.Outlined.Business, null, tint = White, modifier = Modifier.size(40.dp))
                        }
                    }
                }
                if (!isUploading) {
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
            Box(
                modifier = androidx.compose.ui.Modifier.clip(RoundedCornerShape(20.dp)).background(Teal500.copy(alpha = 0.25f)).padding(horizontal = 12.dp, vertical = 5.dp)
            ) { Text("Empresa", style = MaterialTheme.typography.labelMedium, color = Teal200) }
        }
    }
}

@Composable
private fun EmpresaSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Icon(icon, null, tint = Teal500, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
            }
            HorizontalDivider(color = Gray200.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
private fun EmpresaField(
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
private fun AdnCorporativoSection(radarData: RadarData?) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            "ADN Corporativo",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Completa tu perfil de cultura para visualizar tu ADN organizacional",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
        if (radarData != null && radarData.ejes.any { it.valor > 0 }) {
            Spacer(Modifier.height(4.dp))
            RadarChartView(
                data = radarData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CulturaSection(
    giro: String,
    onGiroChange: (String) -> Unit,
    tipoTrabajador: String,
    onTipoChange: (String) -> Unit,
    habilidadValorada: String,
    onHabilidadChange: (String) -> Unit,
    ritmo: String,
    onRitmoChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text("Perfil y Cultura", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)

        var giroExpanded by remember { mutableStateOf(false) }
        var tipoExpanded by remember { mutableStateOf(false) }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ExposedDropdownMenuBox(expanded = giroExpanded, onExpandedChange = { giroExpanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = giro.ifBlank { "Giro de empresa..." },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("1. Giro principal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(giroExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(expanded = giroExpanded, onDismissRequest = { giroExpanded = false }) {
                    GIROS.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { onGiroChange(g); giroExpanded = false }) }
                }
            }

            ExposedDropdownMenuBox(expanded = tipoExpanded, onExpandedChange = { tipoExpanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = tipoTrabajador.ifBlank { "Tipo..." },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("2. Tipo trabajador") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tipoExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(expanded = tipoExpanded, onDismissRequest = { tipoExpanded = false }) {
                    TIPOS_TRABAJADOR.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { onTipoChange(t); tipoExpanded = false }) }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("3. ¿Qué habilidad valoras más?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HABILIDADES_VALORADAS.forEach { opt ->
                    val selected = habilidadValorada == opt
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal500 else Gray100,
                        modifier = Modifier.clickable { onHabilidadChange(if (selected) "" else opt) }
                    ) {
                        Text(opt, style = MaterialTheme.typography.labelSmall, color = if (selected) White else Gray700, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("4. ¿Cómo es el ritmo de trabajo?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Gray700)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RITMOS.forEach { opt ->
                    val selected = ritmo == opt
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Teal50 else Gray100,
                        modifier = Modifier.clickable { onRitmoChange(if (selected) "" else opt) }
                    ) {
                        Text(opt, style = MaterialTheme.typography.labelSmall, color = if (selected) Teal500 else Gray700, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DocLegalSection(docUrl: String?, isUploading: Boolean, onPickDoc: () -> Unit) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Documentación Legal", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Icon(
                    imageVector = if (isUploading) Icons.Outlined.UploadFile else Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Teal500,
                    modifier = Modifier.size(32.dp)
                )
                Text("Estatutos / RUT de la Empresa", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Text("Sube un documento que acredite la organización (PDF o imagen)", style = MaterialTheme.typography.bodySmall, color = Gray500)
                if (docUrl != null) {
                    TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(docUrl))) }) {
                        Icon(Icons.Outlined.Description, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ver documento", color = Teal500, style = MaterialTheme.typography.labelMedium)
                    }
                }
                OutlinedButton(
                    onClick = onPickDoc,
                    enabled = !isUploading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal500),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Teal500.copy(alpha = 0.5f))
                ) {
                    if (isUploading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Teal500)
                    else Icon(Icons.Outlined.UploadFile, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (docUrl != null) "Cambiar Documento" else "Subir Documento", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmpresaBankSection(
    bankName: String,
    onBankChange: (String) -> Unit,
    accountType: String,
    onAccountTypeChange: (String) -> Unit,
    accountNumber: String,
    onAccountNumberChange: (String) -> Unit
) {
    EmpresaSectionCard(title = "Información para Pagos", icon = Icons.Outlined.AccountBalance) {
        var bancosExpanded by remember { mutableStateOf(false) }
        var tipoExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded = bancosExpanded, onExpandedChange = { bancosExpanded = it }) {
            OutlinedTextField(
                value = bankName.ifBlank { "Seleccionar banco..." },
                onValueChange = {},
                readOnly = true,
                label = { Text("Banco") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bancosExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(expanded = bancosExpanded, onDismissRequest = { bancosExpanded = false }) {
                BANCOS.forEach { b -> DropdownMenuItem(text = { Text(b) }, onClick = { onBankChange(b); bancosExpanded = false }) }
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
                TIPOS_CUENTA.forEach { (v, l) -> DropdownMenuItem(text = { Text(l) }, onClick = { onAccountTypeChange(v); tipoExpanded = false }) }
            }
        }

        OutlinedTextField(
            value = accountNumber,
            onValueChange = onAccountNumberChange,
            label = { Text("Número de cuenta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun EmpresaDeleteDialog(
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
        title = { Text("¿Eliminar cuenta de empresa?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "Esta acción eliminará permanentemente la cuenta y todas las ofertas publicadas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña de administrador") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    isError = error != null
                )
                if (error != null) Text(error, style = MaterialTheme.typography.labelSmall, color = Error600)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting, colors = ButtonDefaults.textButtonColors(contentColor = Error600)) {
                if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Error600)
                else Text("Eliminar empresa")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun createLogoUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "images").also { it.mkdirs() }
    val file = File(dir, "logo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
