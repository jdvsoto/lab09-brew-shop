package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.model.PaymentMethod

private const val FOLIO_DIGITS = 5

/**
 * Folio de la orden numero [orderNumber]: 1 da "#ORD-00001".
 *
 * Se usa padStart y no String.format porque format depende del idioma del telefono y en
 * algunos idiomas cambia los digitos.
 */
fun formatOrderFolio(orderNumber: Int): String =
    "#ORD-" + orderNumber.toString().padStart(FOLIO_DIGITS, '0')

/**
 * Arma el recibo inmutable de una compra. En CF el NIT y la razon social quedan en null
 * aunque se hayan escrito: no forman parte de una compra como Consumidor Final.
 */
fun createOrderReceipt(
    orderNumber: Int,
    customerName: String,
    phone: String,
    billingType: BillingType,
    nit: String,
    businessName: String,
    paymentMethod: PaymentMethod,
    unitCount: Int,
    totalCents: Int
): OrderReceipt {
    val isNitBilling = billingType == BillingType.NIT
    return OrderReceipt(
        folio = formatOrderFolio(orderNumber),
        customerName = customerName.trim(),
        phone = phone.trim(),
        billingType = billingType,
        nit = if (isNitBilling) nit.trim() else null,
        businessName = if (isNitBilling) businessName.trim() else null,
        paymentMethod = paymentMethod,
        unitCount = unitCount,
        totalCents = totalCents
    )
}
