package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.OrderLine

/**
 * Resultado de una operacion sobre el pedido.
 *
 * Distinguir exito de rechazo con motivo permite que el ViewModel comunique a la UI que
 * paso, y que un rechazo no tenga forma de modificar el pedido: [Rejected] ni siquiera
 * transporta lineas.
 */
sealed interface OrderResult {
    data class Success(val lines: List<OrderLine>, val message: String) : OrderResult

    data class Rejected(val reason: String) : OrderResult
}

/** Una linea del pedido resuelta contra el catalogo, lista para mostrarse. */
data class OrderItem(
    val product: Coffee,
    val quantity: Int,
    val subtotalCents: Int
)

/**
 * Agrega [quantity] unidades de [productId] al pedido.
 *
 * Es la regla de negocio del laboratorio y es independiente de Compose: valida aunque se
 * invoque sin botones ni pantalla. Rechaza, sin tocar el pedido, cuando la cantidad no es
 * positiva, cuando el producto no existe en el catalogo, cuando esta agotado y cuando la
 * cantidad acumulada superaria las existencias. Deshabilitar un boton en la UI no
 * sustituye esta validacion.
 *
 * Preparar el pedido no descuenta inventario: [Coffee.stock] es el limite, no un saldo.
 */
fun addToOrder(
    lines: List<OrderLine>,
    products: List<Coffee>,
    productId: String,
    quantity: Int = 1
): OrderResult {
    if (quantity <= 0) {
        return OrderResult.Rejected("La cantidad debe ser mayor que cero. El pedido no cambio.")
    }

    val product = products.firstOrNull { it.id == productId }
        ?: return OrderResult.Rejected("El producto no existe en el catalogo. El pedido no cambio.")

    if (product.stock <= 0) {
        return OrderResult.Rejected("${product.name} esta agotado. El pedido no cambio.")
    }

    val currentQuantity = quantityOf(lines, productId)
    val newQuantity = currentQuantity + quantity
    if (newQuantity > product.stock) {
        return OrderResult.Rejected(
            "Solo hay ${product.stock} unidades disponibles. El pedido no cambio."
        )
    }

    // El mismo producto acumula en su linea existente en lugar de abrir otra.
    val updatedLines = if (currentQuantity == 0) {
        lines + OrderLine(productId = productId, quantity = quantity)
    } else {
        lines.map { line ->
            if (line.productId == productId) line.copy(quantity = newQuantity) else line
        }
    }

    val unitLabel = if (quantity == 1) "unidad" else "unidades"
    return OrderResult.Success(
        lines = updatedLines,
        message = "Se agrego $quantity $unitLabel al pedido."
    )
}

/**
 * Baja en una unidad la linea de [productId]. Llegar a cero elimina la linea completa y
 * no altera las demas.
 */
fun decreaseOrderLine(lines: List<OrderLine>, productId: String): List<OrderLine> {
    val line = lines.firstOrNull { it.productId == productId } ?: return lines
    return if (line.quantity <= 1) {
        removeOrderLine(lines, productId)
    } else {
        lines.map { current ->
            if (current.productId == productId) current.copy(quantity = current.quantity - 1) else current
        }
    }
}

/** Elimina la linea de [productId]. Las demas lineas quedan intactas. */
fun removeOrderLine(lines: List<OrderLine>, productId: String): List<OrderLine> =
    lines.filterNot { it.productId == productId }

/** Unidades acumuladas de un producto dentro del pedido. Cero si no esta. */
fun quantityOf(lines: List<OrderLine>, productId: String): Int =
    lines.firstOrNull { it.productId == productId }?.quantity ?: 0

/** Total de unidades del pedido, que es lo que muestra el contador del catalogo. */
fun orderUnitCount(lines: List<OrderLine>): Int = lines.sumOf { it.quantity }

/** Subtotal de una linea. Unico lugar donde se multiplica precio por cantidad. */
fun lineSubtotalCents(product: Coffee, quantity: Int): Int = product.priceCents * quantity

/**
 * Resuelve las lineas contra el catalogo. Una linea cuyo producto ya no exista se
 * descarta en lugar de romper el calculo.
 */
fun orderItems(lines: List<OrderLine>, products: List<Coffee>): List<OrderItem> =
    lines.mapNotNull { line ->
        val product = products.firstOrNull { it.id == line.productId } ?: return@mapNotNull null
        OrderItem(
            product = product,
            quantity = line.quantity,
            subtotalCents = lineSubtotalCents(product, line.quantity)
        )
    }

/**
 * Total del pedido. Se calcula sumando los mismos subtotales que muestra la pantalla,
 * de modo que no puedan diferir entre si.
 */
fun orderTotalCents(lines: List<OrderLine>, products: List<Coffee>): Int =
    orderItems(lines, products).sumOf { it.subtotalCents }

/**
 * Formatea centavos como importe de la tienda, con dos decimales y siempre el mismo
 * simbolo. Trabaja con enteros para no arrastrar errores de punto flotante y no depende
 * de la configuracion regional del dispositivo.
 */
fun formatPriceCents(cents: Int): String {
    val units = cents / 100
    val remainder = (cents % 100).toString().padStart(2, '0')
    return "Q$units.$remainder"
}

/**
 * Filtra el catalogo por nombre, ignorando mayusculas y espacios exteriores. Una consulta
 * vacia devuelve el catalogo completo en su orden original.
 */
fun filterProductsByName(products: List<Coffee>, query: String): List<Coffee> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return products
    return products.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
}
