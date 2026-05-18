package cl.catchgo.app.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.catchgo.app.ui.theme.BrandBlue50
import cl.catchgo.app.ui.theme.BrandBlue600
import cl.catchgo.app.ui.theme.BrandBlue700
import cl.catchgo.app.ui.theme.Gray100
import cl.catchgo.app.ui.theme.Gray200
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Gray700
import cl.catchgo.app.ui.theme.Spacing
import cl.catchgo.app.ui.theme.Teal500
import cl.catchgo.app.ui.theme.White

enum class HomeSection {
    INICIO, QUIENES_SOMOS, CONTACTO, TERMINOS
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableStateOf(HomeSection.INICIO) }

    // Intercept back press when inside a sub-section
    BackHandler(enabled = activeSection != HomeSection.INICIO) {
        activeSection = HomeSection.INICIO
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (activeSection) {
            HomeSection.INICIO -> SectionInicio(onNavigate = { activeSection = it })
            HomeSection.QUIENES_SOMOS -> SectionQuienesSomos(onBack = { activeSection = HomeSection.INICIO })
            HomeSection.CONTACTO -> SectionContacto(onBack = { activeSection = HomeSection.INICIO })
            HomeSection.TERMINOS -> SectionTerminos(onBack = { activeSection = HomeSection.INICIO })
        }
    }
}

// 1. WELCOME SECTION (INICIO)
@Composable
private fun SectionInicio(
    onNavigate: (HomeSection) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Section with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandBlue700, BrandBlue600)
                    )
                )
                .padding(horizontal = Spacing.lg, vertical = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = Spacing.md)
                ) {
                    Text(
                        text = "Catch&Go",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = White,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }

                Text(
                    text = "Conexiones laborales instantáneas al alcance de tu mano",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = White,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = "Conectamos empresas que necesitan cubrir turnos urgentes con los mejores trabajadores disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Value Propositions
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Nuestros Pilares",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                PilarCard(
                    title = "Agilidad",
                    desc = "Encuentra y toma turnos de forma inmediata con un par de clics.",
                    icon = Icons.Outlined.Shield,
                    tint = Teal500,
                    modifier = Modifier.weight(1f)
                )
                PilarCard(
                    title = "Confianza",
                    desc = "Perfiles 100% verificados y pagos en línea rápidos y seguros.",
                    icon = Icons.Outlined.Lock,
                    tint = BrandBlue600,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Navigation Options (List)
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.lg)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Información General",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )

            MenuNavigationRow(
                title = "¿Quiénes Somos?",
                subtitle = "Conoce nuestro propósito y visión 2027",
                icon = Icons.Outlined.Groups,
                onClick = { onNavigate(HomeSection.QUIENES_SOMOS) }
            )

            MenuNavigationRow(
                title = "Contacto & Soporte",
                subtitle = "¿Tienes dudas? Escríbenos directamente",
                icon = Icons.Outlined.Email,
                onClick = { onNavigate(HomeSection.CONTACTO) }
            )

            MenuNavigationRow(
                title = "Términos y Condiciones",
                subtitle = "Reglas, derechos y responsabilidades de uso",
                icon = Icons.Outlined.Scale,
                onClick = { onNavigate(HomeSection.TERMINOS) }
            )
        }
    }
}

// 2. QUIENES SOMOS SECTION
@Composable
private fun SectionQuienesSomos(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SubHeaderRow(title = "¿Quiénes Somos?", onBack = onBack)

        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Gray200),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "En Catch&Go, entendemos que el mundo laboral es dinámico. Nuestra misión es conectar de manera rápida, segura y transparente a empresas que necesitan cubrir urgencias operativas con trabajadores dispuestos a tomar turnos ocasionales.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = Gray700
                    )
                }
            }

            Text(
                text = "Nuestra Visión",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Gray200),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "Ser la principal plataforma de matching laboral para trabajos temporales en Chile, reduciendo la fricción al mínimo para que la contratación ocasional sea tan sencilla como un clic.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = Gray700
                    )
                }
            }

            Text(
                text = "Nuestros Valores",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700,
                modifier = Modifier.padding(top = Spacing.sm)
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                ValorItem(title = "Transparencia", desc = "Ofertas y remuneraciones claras desde el primer momento.")
                ValorItem(title = "Agilidad", desc = "Respuestas en minutos para resolver urgencias operativas.")
                ValorItem(title = "Confianza", desc = "Validación rigurosa de antecedentes y perfiles de usuarios.")
                ValorItem(title = "Accesibilidad", desc = "Una interfaz simple e intuitiva para todas las edades.")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 3. CONTACTO SECTION
@Composable
private fun SectionContacto(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SubHeaderRow(title = "Contacto & Soporte", onBack = onBack)

        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (submitted) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Teal500,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "¡Mensaje Enviado!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = "Hemos recibido tu consulta. Nos pondremos en contacto contigo en breve.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Formulario de Consultas",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Gray200),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Name Field
                    Column {
                        Text(
                            text = "NOMBRE COMPLETO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Gray500,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = null
                            },
                            placeholder = { Text("Ej. Juan Pérez") },
                            isError = nameError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue600,
                                unfocusedBorderColor = Gray200
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = Spacing.xs)
                            )
                        }
                    }

                    // Email Field
                    Column {
                        Text(
                            text = "CORREO ELECTRÓNICO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Gray500,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            placeholder = { Text("tucorreo@ejemplo.com") },
                            isError = emailError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue600,
                                unfocusedBorderColor = Gray200
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (emailError != null) {
                            Text(
                                text = emailError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = Spacing.xs)
                            )
                        }
                    }

                    // Message Field
                    Column {
                        Text(
                            text = "MENSAJE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Gray500,
                            modifier = Modifier.padding(bottom = Spacing.xs)
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = {
                                message = it
                                messageError = null
                            },
                            placeholder = { Text("¿En qué podemos ayudarte?") },
                            isError = messageError != null,
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue600,
                                unfocusedBorderColor = Gray200
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (messageError != null) {
                            Text(
                                text = messageError.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = Spacing.xs)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    // Submit Button
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "El nombre es obligatorio"
                                hasError = true
                            }
                            if (email.isBlank()) {
                                emailError = "El correo es obligatorio"
                                hasError = true
                            } else if (!email.contains("@")) {
                                emailError = "Formato de correo inválido"
                                hasError = true
                            }
                            if (message.isBlank()) {
                                messageError = "El mensaje es obligatorio"
                                hasError = true
                            }

                            if (!hasError) {
                                isSubmitting = true
                                // Simulate network delay
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isSubmitting = false
                                    submitted = true
                                    name = ""
                                    email = ""
                                    message = ""
                                }, 1500)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue600),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Enviar Mensaje",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 4. TERMINOS Y CONDICIONES SECTION
@Composable
private fun SectionTerminos(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SubHeaderRow(title = "Términos y Condiciones", onBack = onBack)

        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Última actualización: 10 de Mayo, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )

            TerminosCard(
                num = "1",
                title = "Aceptación de los Términos",
                desc = "Al acceder y utilizar la plataforma Catch-Go, usted acepta estar sujeto a estos Términos y Condiciones de Uso. Catch-Go actúa como un intermediario tecnológico para facilitar la conexión rápida y directa entre empresas y trabajadores."
            )

            TerminosCard(
                num = "2",
                title = "Responsabilidades del Usuario",
                desc = "Tanto trabajadores como empresas se comprometen a proporcionar información verídica y actualizada.\n\n• Trabajadores: Son responsables de cumplir puntualmente con los turnos aceptados.\n• Empresas: Son responsables de garantizar condiciones laborales seguras y cumplir con los pagos acordados."
            )

            TerminosCard(
                num = "3",
                title = "Privacidad y Datos",
                desc = "La seguridad de sus datos es nuestra prioridad. Catch-Go utiliza servicios de bases de datos y autenticación seguros. No compartiremos su información personal con terceros fuera del matching laboral sin su consentimiento."
            )

            TerminosCard(
                num = "4",
                title = "Limitación de Responsabilidad",
                desc = "Catch-Go no es empleador directo de los trabajadores. La relación comercial/laboral se establece de forma directa entre la empresa y el trabajador ocasional."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// REUSABLE HELPER UI WIDGETS
@Composable
private fun PilarCard(
    title: String,
    desc: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Gray200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
    }
}

@Composable
private fun MenuNavigationRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(BrandBlue50, CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandBlue600,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray500
            )
        }
    }
}

@Composable
private fun SubHeaderRow(
    title: String,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(bottom = 1.dp, color = Gray200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = BrandBlue700
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }
    }
}

@Composable
private fun ValorItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(Teal500, CircleShape)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = BrandBlue700
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }
    }
}

@Composable
private fun TerminosCard(
    num: String,
    title: String,
    desc: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Gray200),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrandBlue600)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$num. $title",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = White
                    )
                }
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Gray700,
                modifier = Modifier.padding(Spacing.lg)
            )
        }
    }
}
