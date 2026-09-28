package gt.uvg.brewshop.model

/**
 * Una linea del pedido: un producto y la cantidad acumulada de ese producto.
 *
 * Guarda solo el identificador, no una copia del [Coffee]. Asi el nombre, el precio y
 * las existencias siempre se leen del catalogo y no pueden quedar desactualizados.
 * Agregar el mismo producto varias veces aumenta [quantity] en la linea existente en
 * lugar de crear otra.
 */
data class OrderLine(
    val productId: String,
    val quantity: Int
)
