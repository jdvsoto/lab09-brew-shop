package gt.uvg.brewshop.ui.screens.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.components.displayName
import gt.uvg.brewshop.ui.store.CheckoutUiState
import gt.uvg.brewshop.ui.store.FormField
import gt.uvg.brewshop.ui.theme.BrewShopTheme

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneFocusRequester = remember { FocusRequester() }
    val nitFocusRequester = remember { FocusRequester() }
    val businessNameFocusRequester = remember { FocusRequester() }
    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    StoreScaffold(title = "Checkout", onBack = onBack, modifier = modifier) { innerPadding ->
        Column(
            // imePadding va antes del scroll para que el area visible se achique con el
            // teclado abierto y el scroll alcance los ultimos campos y el boton.
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Total: ${formatPriceCents(totalCents)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OrderSummaryCard(orderItems = orderItems, orderUnitCount = orderUnitCount)

            OutlinedTextField(
                value = uiState.fullName.value,
                onValueChange = onFullNameChange,
                label = { Text("Nombre completo *") },
                placeholder = { Text("Ej. María Morales") },
                singleLine = true,
                isError = uiState.visibleFullNameError != null,
                supportingText = uiState.visibleFullNameError?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { phoneFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.phone.value,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono / WhatsApp *") },
                placeholder = { Text("Ej. 55123456") },
                singleLine = true,
                isError = uiState.visiblePhoneError != null,
                supportingText = uiState.visiblePhoneError?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = if (uiState.isNitBilling) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    // El campo NIT solo existe en NIT: pedirle foco en CF cerraria la app.
                    onNext = { if (uiState.isNitBilling) nitFocusRequester.requestFocus() },
                    onDone = { hideKeyboard() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(phoneFocusRequester)
            )

            SectionTitle("Facturación *")
            Column(modifier = Modifier.selectableGroup()) {
                BillingType.entries.forEach { type ->
                    RadioOptionRow(
                        label = type.displayName(),
                        selected = uiState.billingType == type,
                        onClick = {
                            // Primero se suelta el foco: asi el campo NIT no se oculta con
                            // el foco adentro al volver a CF.
                            hideKeyboard()
                            onBillingTypeChange(type)
                        }
                    )
                }
            }

            AnimatedVisibility(visible = uiState.isNitBilling) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DATOS DE FACTURACIÓN FISCAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = uiState.nit.value,
                        onValueChange = onNitChange,
                        label = { Text("NIT *") },
                        singleLine = true,
                        isError = uiState.visibleNitError != null,
                        supportingText = uiState.visibleNitError?.let { error -> { Text(error) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { businessNameFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nitFocusRequester)
                    )
                    OutlinedTextField(
                        value = uiState.businessName.value,
                        onValueChange = onBusinessNameChange,
                        label = { Text("Razón Social / Nombre fiscal *") },
                        placeholder = { Text("Ej. Guzmán Inversiones S.A.") },
                        singleLine = true,
                        isError = uiState.visibleBusinessNameError != null,
                        supportingText = uiState.visibleBusinessNameError?.let { error ->
                            { Text(error) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { hideKeyboard() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(businessNameFocusRequester)
                    )
                }
            }

            SectionTitle("Método de Pago *")
            Column(modifier = Modifier.selectableGroup()) {
                PaymentMethod.entries.forEach { method ->
                    RadioOptionRow(
                        label = method.displayName(),
                        selected = uiState.paymentMethod == method,
                        onClick = {
                            hideKeyboard()
                            onPaymentMethodChange(method)
                        }
                    )
                }
            }

            Button(
                onClick = onConfirmOrder,
                enabled = isConfirmEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar pedido (Total ${formatPriceCents(totalCents)})")
            }

            if (!isConfirmEnabled) {
                Text(
                    text = if (orderUnitCount == 0) {
                        "Tu pedido no tiene productos."
                    } else {
                        "Completa los campos obligatorios para continuar."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    orderItems: List<OrderItem>,
    orderUnitCount: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Resumen del pedido",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$orderUnitCount unidades",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            orderItems.forEach { item ->
                Text(
                    text = "${item.product.name} (x${item.quantity}) · Subtotal: " +
                        formatPriceCents(item.subtotalCents),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Opcion de un grupo de radios. El objetivo tactil es la fila entera, de al menos 48 dp:
 * por eso el RadioButton no tiene onClick propio. Si lo tuviera habria dos objetivos
 * superpuestos y TalkBack anunciaria dos veces la misma opcion.
 */
@Composable
private fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
}

private val previewCoffee = Coffee(
    id = "c1",
    name = "Bourbon Antigua",
    description = "Cuerpo redondo y dulzor de panela.",
    priceCents = 7200,
    stock = 12,
    imageUrl = "https://picsum.photos/seed/c1/400/400",
    roasterId = "preview-roaster",
    origin = "Antigua Guatemala",
    altitude = "1,550 msnm",
    process = "Lavado",
    roastLevel = "Tueste medio",
    tastingNotes = "Panela y cacao"
)

private val previewItem = OrderItem(product = previewCoffee, quantity = 2, subtotalCents = 14400)

@Composable
private fun CheckoutScreenPreviewContent(
    uiState: CheckoutUiState,
    orderItems: List<OrderItem>,
    orderUnitCount: Int,
    totalCents: Int,
    isConfirmEnabled: Boolean
) {
    BrewShopTheme {
        CheckoutScreen(
            uiState = uiState,
            orderItems = orderItems,
            orderUnitCount = orderUnitCount,
            totalCents = totalCents,
            isConfirmEnabled = isConfirmEnabled,
            onFullNameChange = {},
            onPhoneChange = {},
            onBillingTypeChange = {},
            onNitChange = {},
            onBusinessNameChange = {},
            onPaymentMethodChange = {},
            onConfirmOrder = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CheckoutScreenCfPreview() {
    CheckoutScreenPreviewContent(
        uiState = CheckoutUiState(),
        orderItems = listOf(previewItem),
        orderUnitCount = 2,
        totalCents = 14400,
        isConfirmEnabled = false
    )
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun CheckoutScreenNitErrorPreview() {
    CheckoutScreenPreviewContent(
        uiState = CheckoutUiState(
            fullName = FormField("Alberto Guzmán", isTouched = true),
            phone = FormField("55442211", isTouched = true),
            billingType = BillingType.NIT,
            nit = FormField("4512", isTouched = true)
        ),
        orderItems = listOf(previewItem),
        orderUnitCount = 2,
        totalCents = 14400,
        isConfirmEnabled = false
    )
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun CheckoutScreenEmptyOrderPreview() {
    CheckoutScreenPreviewContent(
        uiState = CheckoutUiState(),
        orderItems = emptyList(),
        orderUnitCount = 0,
        totalCents = 0,
        isConfirmEnabled = false
    )
}
