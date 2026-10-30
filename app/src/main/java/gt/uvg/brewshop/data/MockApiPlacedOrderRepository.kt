package gt.uvg.brewshop.data

import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.PlacedOrder
import gt.uvg.brewshop.network.OrderApi
import gt.uvg.brewshop.network.buildNewOrderDto
import gt.uvg.brewshop.network.toPlacedOrder
import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

private const val HTTP_NOT_FOUND = 404
private const val ORDERS_SORT_FIELD = "createdAt"
private const val ORDERS_SORT_DIRECTION = "desc"

/** Los DTO se quedan aqui: hacia afuera solo salen modelos de dominio. */
class MockApiPlacedOrderRepository(private val orderApi: OrderApi) : PlacedOrderRepository {

    override suspend fun placeOrder(
        items: List<OrderItem>,
        billingType: BillingType,
        paymentMethod: PaymentMethod
    ): Result<String> = request {
        val newOrder = buildNewOrderDto(
            items = items,
            billingType = billingType,
            paymentMethod = paymentMethod,
            createdAt = System.currentTimeMillis()
        )
        orderApi.createOrder(newOrder).id
    }

    override suspend fun getOrders(): Result<List<PlacedOrder>> {
        val result = request {
            orderApi.getOrders(sortBy = ORDERS_SORT_FIELD, order = ORDERS_SORT_DIRECTION)
                .map { it.toPlacedOrder() }
        }
        // MockAPI responde 404 en vez de una lista vacia cuando no hay coincidencias.
        val isEmptyResource = result.exceptionOrNull().let {
            it is DataException && it.reason == DataErrorReason.NOT_FOUND
        }
        return if (isEmptyResource) Result.success(emptyList()) else result
    }

    override suspend fun getOrder(orderId: String): Result<PlacedOrder> = request {
        orderApi.getOrder(orderId).toPlacedOrder()
    }

    private suspend fun <T> request(call: suspend () -> T): Result<T> = try {
        Result.success(call())
    } catch (e: IOException) {
        Result.failure(DataException(DataErrorReason.NO_CONNECTION, e))
    } catch (e: HttpException) {
        val reason = if (e.code() == HTTP_NOT_FOUND) DataErrorReason.NOT_FOUND else DataErrorReason.SERVER
        Result.failure(DataException(reason, e))
    } catch (e: SerializationException) {
        Result.failure(DataException(DataErrorReason.UNEXPECTED_RESPONSE, e))
    }
}
