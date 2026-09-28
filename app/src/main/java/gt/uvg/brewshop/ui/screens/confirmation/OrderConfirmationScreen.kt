package gt.uvg.brewshop.ui.screens.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.ui.components.StoreScaffold

/** Confirmacion de la compra. Todo lo que muestra sale del recibo guardado, no del carrito. */
@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cuerpo provisional de la Parte 1: la Parte 3 lo reemplaza sin cambiar la firma.
    StoreScaffold(title = "Pedido confirmado", modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("¡Pedido confirmado!", style = MaterialTheme.typography.headlineSmall)
            Text("Folio: ${receipt.folio}")
            Text("Total del pedido: ${formatPriceCents(receipt.totalCents)}")
            Button(onClick = onBackToCatalog, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al catálogo")
            }
        }
    }
}
