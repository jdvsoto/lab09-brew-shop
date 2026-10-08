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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.components.displayName
import gt.uvg.brewshop.ui.theme.BrewShopTheme

/**
 * Confirmacion de la compra.
 *
 * Todo lo que muestra sale de [receipt], el recibo guardado al confirmar. El carrito ya esta
 * vacio en este punto, asi que recalcular desde el pedido daria Q0.00.
 *
 * No tiene flecha de regreso: se sale con "Volver al catalogo" o con Atras, y ninguno de los
 * dos vuelve a abrir el checkout porque ya no esta en la pila.
 */
@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoreScaffold(title = "Pedido confirmado", modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // El titulo ya comunica lo mismo, por eso el icono no lleva descripcion.
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = "¡Pedido confirmado!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Orden registrada exitosamente en su tienda.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReceiptRow(label = "Folio:", value = receipt.folio)
                    ReceiptRow(label = "Cliente:", value = receipt.customerName)
                    ReceiptRow(label = "Teléfono:", value = receipt.phone)
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

            Button(onClick = onBackToCatalog, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al catálogo")
            }
        }
    }
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OrderConfirmationConsumerFinalPreview() {
    BrewShopTheme {
        OrderConfirmationScreen(
            receipt = OrderReceipt(
                folio = "#ORD-00001",
                customerName = "Alberto Guzmán",
                phone = "55442211",
                billingType = BillingType.CF,
                nit = null,
                businessName = null,
                paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
                unitCount = 3,
                totalCents = 6750
            ),
            onBackToCatalog = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OrderConfirmationNitPreview() {
    BrewShopTheme {
        OrderConfirmationScreen(
            receipt = OrderReceipt(
                folio = "#ORD-00002",
                customerName = "Alberto Guzmán",
                phone = "55442211",
                billingType = BillingType.NIT,
                nit = "1234567",
                businessName = "Guzmán Inversiones S.A.",
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                unitCount = 1,
                totalCents = 2250
            ),
            onBackToCatalog = {}
        )
    }
}
