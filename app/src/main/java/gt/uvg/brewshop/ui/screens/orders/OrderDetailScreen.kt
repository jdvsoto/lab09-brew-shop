package gt.uvg.brewshop.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.PlacedOrder
import gt.uvg.brewshop.model.PlacedOrderLine
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.components.displayName
import gt.uvg.brewshop.ui.orders.OrderDetailUiState
import gt.uvg.brewshop.ui.orders.formatOrderDate
import gt.uvg.brewshop.ui.theme.BrewShopTheme
import java.time.ZoneId

/**
 * Detalle de un pedido pedido al servidor por su id. Todo sale del pedido guardado: las
 * lineas traen su propio nombre y precio, asi que no depende del catalogo local.
 */
@Composable
fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    onRetry: () -> Unit,
    onBackToOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoreScaffold(title = "Detalle del pedido", modifier = modifier, onBack = onBackToOrders) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState) {
                OrderDetailUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Cargando el pedido…", style = MaterialTheme.typography.bodyLarge)
                }

                OrderDetailUiState.NotFound -> {
                    Text(
                        text = "Pedido no encontrado",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Este pedido ya no existe en el servidor.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBackToOrders) { Text("Volver a Mis pedidos") }
                }

                is OrderDetailUiState.Error -> {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry) { Text("Reintentar") }
                }

                is OrderDetailUiState.Success -> OrderDetailContent(order = uiState.order)
            }
        }
    }
}

@Composable
private fun OrderDetailContent(order: PlacedOrder, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pedido #${order.id}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatOrderDate(order.createdAt, System.currentTimeMillis(), ZoneId.systemDefault()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                order.lines.forEach { line -> OrderLineRow(line) }
                HorizontalDivider()
                DetailRow(label = "Unidades:", value = order.unitCount.toString())
                DetailRow(label = "Facturación:", value = order.billingType?.displayName() ?: "—")
                DetailRow(label = "Método de pago:", value = order.paymentMethod?.displayName() ?: "—")
                DetailRow(
                    label = "Total del pedido:",
                    value = formatPriceCents(order.totalCents),
                    emphasized = true
                )
            }
        }
    }
}

@Composable
private fun OrderLineRow(line: PlacedOrderLine, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${line.name} (x${line.quantity})", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${formatPriceCents(line.unitPriceCents)} c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = formatPriceCents(line.subtotalCents), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DetailRow(
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
            style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OrderDetailPreview(uiState: OrderDetailUiState) {
    BrewShopTheme {
        OrderDetailScreen(uiState = uiState, onRetry = {}, onBackToOrders = {})
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun OrderDetailSuccessPreview() {
    OrderDetailPreview(
        OrderDetailUiState.Success(
            PlacedOrder(
                id = "12",
                createdAt = System.currentTimeMillis(),
                lines = listOf(
                    PlacedOrderLine("c2", "Geisha Huehuetenango", 14500, 2, 29000),
                    PlacedOrderLine("c1", "Bourbon Antigua", 7200, 1, 7200)
                ),
                unitCount = 3,
                totalCents = 36200,
                billingType = BillingType.CF,
                paymentMethod = PaymentMethod.CASH_ON_DELIVERY
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailNotFoundPreview() {
    OrderDetailPreview(OrderDetailUiState.NotFound)
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailErrorPreview() {
    OrderDetailPreview(OrderDetailUiState.Error("No pudimos cargar el pedido. Revisa tu conexión."))
}
