package gt.uvg.brewshop.data

import gt.uvg.brewshop.model.OrderLine
import kotlinx.coroutines.flow.Flow

/** Pedido en curso guardado en el dispositivo. Solo guarda ids y cantidades. */
interface CurrentOrderRepository {

    fun observeOrderLines(): Flow<List<OrderLine>>

    /** Lectura puntual, para validar contra lo guardado justo antes de escribir. */
    suspend fun getOrderLines(): List<OrderLine>

    /** Crea la linea o cambia su cantidad, sin moverla de lugar. */
    suspend fun saveLine(line: OrderLine): Result<Unit>

    suspend fun removeLine(productId: String): Result<Unit>

    suspend fun clear(): Result<Unit>
}
