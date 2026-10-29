package gt.uvg.brewshop.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import gt.uvg.brewshop.model.OrderLine

/**
 * Una linea del pedido en curso. La clave es el id del producto, de modo que cada producto
 * ocupa una sola linea, igual que en las reglas del pedido.
 */
@Entity(tableName = "order_lines")
data class OrderLineEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

// El dominio sigue trabajando con OrderLine y no conoce Room.
fun OrderLineEntity.toOrderLine(): OrderLine = OrderLine(productId = productId, quantity = quantity)

fun OrderLine.toEntity(): OrderLineEntity = OrderLineEntity(productId = productId, quantity = quantity)
