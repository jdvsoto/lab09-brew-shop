package gt.uvg.brewshop.ui.components

import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod

/** Textos visibles de las opciones. Son presentacion, por eso viven en la UI y no en el modelo. */
fun BillingType.displayName(): String = when (this) {
    BillingType.CF -> "Consumidor Final (CF)"
    BillingType.NIT -> "Factura con NIT"
}

fun PaymentMethod.displayName(): String = when (this) {
    PaymentMethod.CASH_ON_DELIVERY -> "Efectivo contra entrega"
    PaymentMethod.BANK_TRANSFER -> "Transferencia bancaria"
}
