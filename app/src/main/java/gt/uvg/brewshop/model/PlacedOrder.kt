package gt.uvg.brewshop.model

/** Pedido ya enviado al servidor, tal como lo ven las pantallas, sin depender de la red. */
data class PlacedOrder(
    val id: String,
    val createdAt: Long,
    val lines: List<PlacedOrderLine>,
    val unitCount: Int,
    val totalCents: Int,
    val billingType: BillingType?,
    val paymentMethod: PaymentMethod?
)

data class PlacedOrderLine(
    val productId: String,
    val name: String,
    val unitPriceCents: Int,
    val quantity: Int,
    val subtotalCents: Int
)
