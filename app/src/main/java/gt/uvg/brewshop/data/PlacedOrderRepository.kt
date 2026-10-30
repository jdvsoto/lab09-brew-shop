package gt.uvg.brewshop.data

import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.PlacedOrder

/** Pedidos confirmados, guardados en el servidor de MockAPI. */
interface PlacedOrderRepository {

    /** Envia el pedido y devuelve el id que le asigno el servidor. */
    suspend fun placeOrder(
        items: List<OrderItem>,
        billingType: BillingType,
        paymentMethod: PaymentMethod
    ): Result<String>

    /** Pedidos del mas reciente al mas antiguo. El orden lo resuelve el servidor. */
    suspend fun getOrders(): Result<List<PlacedOrder>>

    /** Falla con [gt.uvg.brewshop.model.DataErrorReason.NOT_FOUND] si el pedido ya no existe. */
    suspend fun getOrder(orderId: String): Result<PlacedOrder>
}
