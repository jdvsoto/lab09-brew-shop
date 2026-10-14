package gt.uvg.brewshop.ui.screens.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.components.displayName
import gt.uvg.brewshop.ui.store.OrderSubmissionState
import gt.uvg.brewshop.ui.theme.BrewShopTheme

/**
 * Confirmacion de la compra, que se abre al tocar "Confirmar pedido" y pinta el estado del
 * envio al servidor: progreso, error con reintento o exito.
 *
 * Mientras se envia no hay botones, asi que no hay forma de mandar el pedido dos veces. En el
 * exito, el numero que asigno el servidor sustituye al folio local, y el resto sale del recibo
 * guardado: el carrito ya esta vacio en ese punto.
 */
@Composable
fun OrderConfirmationScreen(
    submission: OrderSubmissionState,
    receipt: OrderReceipt?,
    pendingTotalCents: Int,
    onRetry: () -> Unit,
    onBackToCheckout: () -> Unit,
    onOpenOrders: () -> Unit,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (submission) {
        OrderSubmissionState.Sending -> "Enviando pedido"
        is OrderSubmissionState.Failed -> "Pedido no enviado"
        is OrderSubmissionState.Sent -> "Pedido recibido"
        OrderSubmissionState.Idle -> "Confirmación"
    }

    StoreScaffold(title = title, modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (submission) {
                OrderSubmissionState.Sending -> SendingContent(pendingTotalCents)

                is OrderSubmissionState.Failed -> FailedContent(
                    message = submission.message,
                    onRetry = onRetry,
                    onBackToCheckout = onBackToCheckout
                )

                is OrderSubmissionState.Sent -> SentContent(
                    orderId = submission.orderId,
                    receipt = receipt,
                    onOpenOrders = onOpenOrders,
                    onBackToCatalog = onBackToCatalog
                )

                OrderSubmissionState.Idle -> IdleContent(onBackToCatalog = onBackToCatalog)
            }
        }
    }
}

@Composable
private fun SendingContent(pendingTotalCents: Int) {
    CircularProgressIndicator(modifier = Modifier.size(56.dp))
    Text(
        text = "Enviando pedido…",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Text(
        text = "Total ${formatPriceCents(pendingTotalCents)}",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FailedContent(
    message: String,
    onRetry: () -> Unit,
    onBackToCheckout: () -> Unit
) {
    StatusIcon(Icons.Filled.CloudOff, tinted = false)
    Text(
        text = "No pudimos enviar tu pedido",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Text(
        text = "Tu pedido y tus datos siguen intactos.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
        Text("Reintentar")
    }
    OutlinedButton(onClick = onBackToCheckout, modifier = Modifier.fillMaxWidth()) {
        Text("Volver al checkout")
    }
}

@Composable
private fun SentContent(
    orderId: String,
    receipt: OrderReceipt?,
    onOpenOrders: () -> Unit,
    onBackToCatalog: () -> Unit
) {
    StatusIcon(Icons.Filled.CheckCircle, tinted = true)
    Text(
        text = "¡Pedido recibido!",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    // El numero lo asigna el servidor: es unico y persistente, a diferencia del folio local.
    Text(
        text = "Pedido #$orderId",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    if (receipt != null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow(label = "Cliente:", value = receipt.customerName)
                ReceiptRow(label = "Facturación:", value = receipt.billingType.displayName())
                receipt.nit?.let { nit -> ReceiptRow(label = "NIT:", value = nit) }
                receipt.businessName?.let { businessName ->
                    ReceiptRow(label = "Razón social:", value = businessName)
                }
                ReceiptRow(label = "Método de pago:", value = receipt.paymentMethod.displayName())
                ReceiptRow(label = "Unidades:", value = receipt.unitCount.toString())
                HorizontalDivider()
                ReceiptRow(
                    label = "Total del pedido:",
                    value = formatPriceCents(receipt.totalCents),
                    emphasized = true
                )
            }
        }
    }

    Text(
        text = "Tu pedido quedó en 0 unidades.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Button(onClick = onOpenOrders, modifier = Modifier.fillMaxWidth()) {
        Text("Ver mis pedidos")
    }
    OutlinedButton(onClick = onBackToCatalog, modifier = Modifier.fillMaxWidth()) {
        Text("Volver al catálogo")
    }
}

@Composable
private fun IdleContent(onBackToCatalog: () -> Unit) {
    Text(
        text = "No hay un pedido en curso.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Button(onClick = onBackToCatalog, modifier = Modifier.fillMaxWidth()) {
        Text("Volver al catálogo")
    }
}

// El titulo ya comunica lo mismo, por eso el icono no lleva descripcion.
@Composable
private fun StatusIcon(imageVector: ImageVector, tinted: Boolean, modifier: Modifier = Modifier) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = if (tinted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        modifier = modifier.size(72.dp)
    )
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (emphasized) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private val previewReceipt = OrderReceipt(
    folio = "#ORD-00001",
    customerName = "Ana López",
    phone = "55123456",
    billingType = BillingType.CF,
    nit = null,
    businessName = null,
    paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    unitCount = 3,
    totalCents = 36200
)

@Composable
private fun ConfirmationPreview(submission: OrderSubmissionState) {
    BrewShopTheme {
        OrderConfirmationScreen(
            submission = submission,
            receipt = previewReceipt,
            pendingTotalCents = 36200,
            onRetry = {},
            onBackToCheckout = {},
            onOpenOrders = {},
            onBackToCatalog = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun OrderConfirmationSendingPreview() {
    ConfirmationPreview(OrderSubmissionState.Sending)
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun OrderConfirmationFailedPreview() {
    ConfirmationPreview(
        OrderSubmissionState.Failed("No pudimos enviar tu pedido. Revisa tu conexión.")
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OrderConfirmationSentPreview() {
    ConfirmationPreview(OrderSubmissionState.Sent(orderId = "12"))
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun OrderConfirmationIdlePreview() {
    ConfirmationPreview(OrderSubmissionState.Idle)
}
