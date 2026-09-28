package gt.uvg.brewshop.ui.screens.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.store.CheckoutUiState

/**
 * Checkout. Recibe el estado ya calculado por el ViewModel y devuelve intenciones: no
 * valida ni guarda valores del formulario.
 */
@Composable
fun CheckoutScreen(
    uiState: CheckoutUiState,
    orderItems: List<OrderItem>,
    orderUnitCount: Int,
    totalCents: Int,
    isConfirmEnabled: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirmOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cuerpo provisional de la Parte 1: la Parte 2 lo reemplaza sin cambiar la firma.
    StoreScaffold(title = "Checkout", modifier = modifier, onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total: ${formatPriceCents(totalCents)} · $orderUnitCount unidades",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.fullName.value,
                onValueChange = onFullNameChange,
                label = { Text("Nombre completo *") },
                singleLine = true,
                isError = uiState.visibleFullNameError != null,
                supportingText = uiState.visibleFullNameError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.phone.value,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono / WhatsApp *") },
                singleLine = true,
                isError = uiState.visiblePhoneError != null,
                supportingText = uiState.visiblePhoneError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onConfirmOrder,
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar pedido (Total ${formatPriceCents(totalCents)})")
            }
        }
    }
}
