package cl.catchgo.app.ui.apply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cl.catchgo.app.ui.components.CatchTextField
import cl.catchgo.app.ui.components.PrimaryButton
import cl.catchgo.app.ui.components.SecondaryButton
import cl.catchgo.app.ui.theme.Error600
import cl.catchgo.app.ui.theme.Gray500
import cl.catchgo.app.ui.theme.Spacing

private const val MESSAGE_MAX = 240

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyBottomSheet(
    offerTitle: String,
    company: String,
    isSubmitting: Boolean,
    errorMessage: String?,
    onConfirm: (message: String?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    var message by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = "Postular a esta oferta",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "$offerTitle · $company",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500
            )

            CatchTextField(
                value = message,
                onValueChange = { if (it.length <= MESSAGE_MAX) message = it },
                label = "Mensaje al empleador (opcional)",
                placeholder = "Contale por qué sos el indicado para este turno.",
                supportingText = "${message.length} / $MESSAGE_MAX",
                singleLine = false,
                enabled = !isSubmitting
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Error600
                )
            }

            Spacer(Modifier.height(Spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SecondaryButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Postular ahora",
                    onClick = { onConfirm(message.ifBlank { null }) },
                    enabled = !isSubmitting,
                    loading = isSubmitting,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spacing.md))
        }
    }
}
