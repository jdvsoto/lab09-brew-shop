package gt.uvg.brewshop.network

import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.PlacedOrder
import gt.uvg.brewshop.model.PlacedOrderLine

/**
 * Arma el cuerpo del POST. Copia nombre y precio unitario del producto para que "Mis
 * pedidos" muestre lo que se compro y no el catalogo actual. No recibe datos personales.
 */
fun buildNewOrderDto(
    items: List<OrderItem>,
    billingType: BillingType,
    paymentMethod: PaymentMethod,
    createdAt: Long
): NewOrderDto = NewOrderDto(
    createdAt = createdAt,
    lines = items.map { item ->
        OrderLineDto(
            productId = item.product.id,
            name = item.product.name,
            unitPriceCents = item.product.priceCents,
            quantity = item.quantity,
            subtotalCents = item.subtotalCents
        )
    },
    unitCount = items.sumOf { it.quantity },
    totalCents = items.sumOf { it.subtotalCents },
    billingType = billingType.name,
    paymentMethod = paymentMethod.name
)

/**
 * Convierte el pedido del servidor al modelo de las pantallas. Un tipo de facturacion o
 * metodo de pago desconocido queda en null en lugar de cerrar la app.
 */
fun OrderDto.toPlacedOrder(): PlacedOrder = PlacedOrder(
    id = id,
    createdAt = createdAt,
    lines = lines.map { line ->
        PlacedOrderLine(
            productId = line.productId,
            name = line.name,
            unitPriceCents = line.unitPriceCents,
            quantity = line.quantity,
            subtotalCents = line.subtotalCents
        )
    },
    unitCount = unitCount,
    totalCents = totalCents,
    billingType = BillingType.entries.firstOrNull { it.name == billingType },
    paymentMethod = PaymentMethod.entries.firstOrNull { it.name == paymentMethod }
)
