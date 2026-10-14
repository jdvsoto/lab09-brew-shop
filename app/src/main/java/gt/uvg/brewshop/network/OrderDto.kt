package gt.uvg.brewshop.network

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineDto(
    val productId: String,
    val name: String,
    val unitPriceCents: Int,
    val quantity: Int,
    val subtotalCents: Int
)

/** Cuerpo del POST: sin id, porque lo asigna el servidor. */
@Serializable
data class NewOrderDto(
    val createdAt: Long,
    val lines: List<OrderLineDto>,
    val unitCount: Int,
    val totalCents: Int,
    val billingType: String,
    val paymentMethod: String
)

/** Pedido tal como lo devuelve el servidor. */
@Serializable
data class OrderDto(
    val id: String,
    val createdAt: Long,
    val lines: List<OrderLineDto>,
    val unitCount: Int,
    val totalCents: Int,
    val billingType: String,
    val paymentMethod: String
)
