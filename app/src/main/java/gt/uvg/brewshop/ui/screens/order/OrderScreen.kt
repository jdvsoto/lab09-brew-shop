package gt.uvg.brewshop.ui.screens.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme

/** Area tactil minima para los controles de cada linea del pedido. */
private val CONTROL_SIZE = 48.dp

/**
 * Resumen del pedido.
 *
 * Cada producto ocupa una sola linea con su cantidad acumulada, nunca una entrada repetida:
 * de eso se encarga `addToOrder` en el dominio y aqui solo se pinta el resultado.
 *
 * Los controles no validan nada. Subir una unidad por encima de las existencias se rechaza
 * en el ViewModel, que devuelve el motivo en [message] y deja el pedido intacto.
 */
@Composable
fun OrderScreen(
    items: List<OrderItem>,
    totalCents: Int,
    message: String?,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onMessageShown: () -> Unit,
    onBackToCatalog: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val current = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(current, duration = SnackbarDuration.Short)
        onMessageShown()
    }

    StoreScaffold(
        title = "Tu pedido",
        modifier = modifier,
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (items.isEmpty()) {
                EmptyOrder(
                    onBackToCatalog = onBackToCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.product.id }) { item ->
                        OrderItemRow(
                            item = item,
                            onIncrease = onIncrease,
                            onDecrease = onDecrease,
                            onRemove = onRemove
                        )
                    }
                }
            }

            // El total se calcula a partir de las lineas vigentes, por lo que al vaciar el
            // pedido queda en Q0.00 y no sobrevive ningun importe anterior.
            OrderTotal(totalCents = totalCents)
        }
    }
}

@Composable
private fun EmptyOrder(
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay productos en tu pedido.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackToCatalog) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun OrderTotal(totalCents: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatPriceCents(totalCents),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OrderItemRow(
    item: OrderItem,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val product = item.product

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatPriceCents(item.subtotalCents),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "${formatPriceCents(product.priceCents)} c/u · ${product.stock} disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onDecrease(product.id) },
                    modifier = Modifier.size(CONTROL_SIZE)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Quitar una unidad de ${product.name}"
                    )
                }
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { onIncrease(product.id) },
                    modifier = Modifier.size(CONTROL_SIZE)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Agregar una unidad de ${product.name}"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onRemove(product.id) },
                    modifier = Modifier.size(CONTROL_SIZE)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar ${product.name} del pedido",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun previewCoffee(id: String, name: String, priceCents: Int, stock: Int) = Coffee(
    id = id,
    name = name,
    description = "Cuerpo redondo y dulzor de panela.",
    priceCents = priceCents,
    stock = stock,
    imageUrl = "https://picsum.photos/seed/$id/400/400",
    roasterId = "preview-roaster",
    origin = "Antigua Guatemala",
    altitude = "1,550 msnm",
    process = "Lavado",
    roastLevel = "Tueste medio",
    tastingNotes = "Panela y cacao"
)

@Preview(showBackground = true)
@Composable
private fun OrderScreenPreview() {
    val first = previewCoffee("c1", "Bourbon Antigua", 7200, 12)
    val second = previewCoffee("c2", "Geisha Huehuetenango", 14500, 3)
    BrewShopTheme {
        OrderScreen(
            items = listOf(
                OrderItem(product = first, quantity = 2, subtotalCents = 14400),
                OrderItem(product = second, quantity = 3, subtotalCents = 43500)
            ),
            totalCents = 57900,
            message = null,
            onIncrease = {},
            onDecrease = {},
            onRemove = {},
            onMessageShown = {},
            onBackToCatalog = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderScreenEmptyPreview() {
    BrewShopTheme {
        OrderScreen(
            items = emptyList(),
            totalCents = 0,
            message = null,
            onIncrease = {},
            onDecrease = {},
            onRemove = {},
            onMessageShown = {},
            onBackToCatalog = {},
            onBack = {}
        )
    }
}
