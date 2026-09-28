package gt.uvg.brewshop.model

/**
 * Recibo de una compra confirmada. Se guarda al confirmar y la pantalla de confirmacion lo
 * muestra tal cual, porque para entonces el pedido ya esta vacio.
 *
 * [nit] y [businessName] son null en una compra como Consumidor Final.
 */
data class OrderReceipt(
    val folio: String,
    val customerName: String,
    val phone: String,
    val billingType: BillingType,
    val nit: String?,
    val businessName: String?,
    val paymentMethod: PaymentMethod,
    val unitCount: Int,
    val totalCents: Int
)
