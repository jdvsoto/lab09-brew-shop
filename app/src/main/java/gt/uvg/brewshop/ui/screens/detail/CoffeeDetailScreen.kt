package gt.uvg.brewshop.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.components.OrderAccessAction
import gt.uvg.brewshop.ui.components.ProductImage
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme

/**
 * Detalle de un producto.
 *
 * No tiene ViewModel propio: recibe valores ya resueltos y devuelve intenciones por
 * callback. [quantityInOrder] son las unidades de ESTE producto ya pedidas y
 * [orderUnitCount] el total del pedido, que es lo que muestra el acceso de la barra
 * superior; son numeros distintos y por eso viajan por separado.
 *
 * Agregar al pedido no navega: la confirmacion o el rechazo aparecen aqui mismo como
 * Snackbar y el resumen del pedido solo se abre si la persona lo pide.
 */
@Composable
fun CoffeeDetailScreen(
    coffee: Coffee,
    roasterName: String,
    isFavorite: Boolean,
    quantityInOrder: Int,
    orderUnitCount: Int,
    message: String?,
    onToggleFavorite: () -> Unit,
    onAddToOrder: () -> Unit,
    onMessageShown: () -> Unit,
    onOpenRoaster: () -> Unit,
    onOpenOrder: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // El mensaje se muestra sin abandonar el detalle. Al terminar se avisa al ViewModel
    // para que lo descarte y no reaparezca al rotar o al volver a esta pantalla.
    LaunchedEffect(message) {
        val current = message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(current, duration = SnackbarDuration.Short)
        onMessageShown()
    }

    // Deshabilitar el boton es solo una ayuda visual: quien valida las existencias es
    // addToOrder en el dominio, y lo sigue haciendo aunque este boton no existiera.
    val canAdd = coffee.stock > 0 && quantityInOrder < coffee.stock

    StoreScaffold(
        title = coffee.name,
        modifier = modifier,
        onBack = onBack,
        actions = {
            OrderAccessAction(unitCount = orderUnitCount, onClick = onOpenOrder)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductImage(
                imageUrl = coffee.imageUrl,
                contentDescription = coffee.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
            )
            Text(coffee.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = formatPriceCents(coffee.priceCents),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${coffee.stock} disponibles · $quantityInOrder en el pedido",
                style = MaterialTheme.typography.labelLarge,
                color = if (coffee.stock == 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(coffee.description, style = MaterialTheme.typography.bodyLarge)

            // Accion principal, separada de las secundarias por jerarquia visual.
            Button(
                onClick = onAddToOrder,
                enabled = canAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar al pedido")
            }
            if (!canAdd) {
                Text(
                    text = if (coffee.stock == 0) {
                        "Producto agotado."
                    } else {
                        "Ya tienes las ${coffee.stock} unidades disponibles en el pedido."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedButton(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                Text(if (isFavorite) "Quitar de favoritos" else "Marcar como favorito")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tostador", style = MaterialTheme.typography.labelLarge)
                    Text(roasterName, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onOpenRoaster) {
                        Text("Ver perfil del tostador")
                    }
                }
            }

            // Estado visual propio de esta pantalla: no pertenece al ViewModel.
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Ocultar ficha técnica" else "Ver ficha técnica")
            }
            if (expanded) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Ficha técnica", style = MaterialTheme.typography.titleMedium)
                        Text("Origen: ${coffee.origin}")
                        Text("Altitud: ${coffee.altitude}")
                        Text("Proceso: ${coffee.process}")
                        Text("Nivel de tueste: ${coffee.roastLevel}")
                        Text("Notas de cata: ${coffee.tastingNotes}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoffeeDetailScreenPreview() {
    val coffee = Coffee(
        id = "preview-1",
        name = "Bourbon Antigua",
        description = "Cuerpo redondo y dulzor de panela.",
        priceCents = 7200,
        stock = 12,
        imageUrl = "https://picsum.photos/seed/preview-1/400/400",
        roasterId = "preview-roaster",
        origin = "Antigua Guatemala",
        altitude = "1,550 msnm",
        process = "Lavado",
        roastLevel = "Tueste medio",
        tastingNotes = "Panela y cacao"
    )
    BrewShopTheme {
        CoffeeDetailScreen(
            coffee = coffee,
            roasterName = "Tostaduria La Bendicion",
            isFavorite = true,
            quantityInOrder = 2,
            orderUnitCount = 3,
            message = null,
            onToggleFavorite = {},
            onAddToOrder = {},
            onMessageShown = {},
            onOpenRoaster = {},
            onOpenOrder = {},
            onBack = {}
        )
    }
}
