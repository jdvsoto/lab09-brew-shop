package gt.uvg.brewshop.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import gt.uvg.brewshop.ui.orders.OrdersUiState
import gt.uvg.brewshop.ui.orders.formatOrderDate
import gt.uvg.brewshop.ui.theme.BrewShopTheme
import java.time.ZoneId

/**
 * "Mis pedidos": los pedidos guardados en el servidor. La pantalla no ordena ni pide nada:
 * pinta la lista en el orden en que llego del servidor y avisa con callbacks.
 */
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onOpenOrder: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onGoToCatalog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoreScaffold(
        title = "Mis pedidos",
        modifier = modifier,
        onBack = onBack,
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Actualizar pedidos")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                OrdersUiState.Loading -> CenteredMessage {
                    CircularProgressIndicator()
                    Text("Cargando tus pedidos…", style = MaterialTheme.typography.bodyLarge)
                }

                OrdersUiState.Empty -> CenteredMessage {
                    Text(
                        text = "Aún no has enviado pedidos.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onGoToCatalog) { Text("Ir al catálogo") }
                }

                is OrdersUiState.Error -> CenteredMessage {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRetry) { Text("Reintentar") }
                }

                is OrdersUiState.Success -> {
                    // Se toma una sola vez por composicion para que todas las filas usen el mismo "hoy".
                    val now = System.currentTimeMillis()
                    val zone = ZoneId.systemDefault()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            OrderRow(
                                order = order,
                                dateText = formatOrderDate(order.createdAt, now, zone),
                                onClick = { onOpenOrder(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(
    order: PlacedOrder,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "#${order.id} · $dateText",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${order.unitCount} unidades",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = formatPriceCents(order.totalCents),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CenteredMessage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        content()
    }
}

private fun previewOrder(id: String, createdAt: Long, totalCents: Int) = PlacedOrder(
    id = id,
    createdAt = createdAt,
    lines = listOf(PlacedOrderLine("c1", "Bourbon Antigua", 7200, 1, 7200)),
    unitCount = 1,
    totalCents = totalCents,
    billingType = BillingType.CF,
    paymentMethod = PaymentMethod.CASH_ON_DELIVERY
)

@Composable
private fun OrdersPreview(uiState: OrdersUiState) {
    BrewShopTheme {
        OrdersScreen(
            uiState = uiState,
            onOpenOrder = {},
            onRetry = {},
            onRefresh = {},
            onGoToCatalog = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrdersSuccessPreview() {
    val now = System.currentTimeMillis()
    OrdersPreview(
        OrdersUiState.Success(
            listOf(
                previewOrder("12", now, 9340),
                previewOrder("11", now - 86_400_000L, 2250),
                previewOrder("10", now - 3 * 86_400_000L, 5180)
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun OrdersEmptyPreview() {
    OrdersPreview(OrdersUiState.Empty)
}

@Preview(showBackground = true)
@Composable
private fun OrdersErrorPreview() {
    OrdersPreview(OrdersUiState.Error("No pudimos cargar tus pedidos. Revisa tu conexión."))
}

@Preview(showBackground = true)
@Composable
private fun OrdersLoadingPreview() {
    OrdersPreview(OrdersUiState.Loading)
}
