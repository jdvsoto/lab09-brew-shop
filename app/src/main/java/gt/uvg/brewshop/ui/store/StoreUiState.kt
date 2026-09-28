package gt.uvg.brewshop.ui.store

import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.domain.orderItems
import gt.uvg.brewshop.domain.orderTotalCents
import gt.uvg.brewshop.domain.orderUnitCount
import gt.uvg.brewshop.domain.quantityOf
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.OrderLine
import gt.uvg.brewshop.model.Roaster

/**
 * Estado inmutable que comparten las pantallas de la tienda.
 *
 * [products] es el catalogo completo y la unica fuente de los datos de producto.
 * [visibleProducts] no es una segunda fuente de verdad, sino el resultado ya calculado de
 * aplicar [query] sobre [products]: se guarda para no volver a filtrar 500 elementos en
 * cada recomposicion de la cuadricula.
 *
 * [favoriteIds] y [orderLines] guardan unicamente identificadores, de modo que saber si un
 * producto esta marcado o cuantas unidades lleva no obliga a copiar esa informacion dentro
 * del producto ni dentro de la pantalla.
 */
data class StoreUiState(
    val products: List<Coffee> = emptyList(),
    val roasters: List<Roaster> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val query: String = "",
    val visibleProducts: List<Coffee> = emptyList(),
    val orderLines: List<OrderLine> = emptyList(),
    val message: String? = null
) {
    /** Cuantos productos tiene el catalogo completo, para el contador "N de M". */
    val catalogSize: Int get() = products.size

    /** Cuantos productos pasan el filtro actual. */
    val resultCount: Int get() = visibleProducts.size

    /** True cuando hay una busqueda activa que no encontro nada. */
    val hasNoResults: Boolean get() = query.isNotBlank() && visibleProducts.isEmpty()

    /** Unidades totales del pedido, que es lo que muestra el acceso al pedido. */
    val orderUnitCount: Int get() = orderUnitCount(orderLines)

    /** Lineas del pedido resueltas contra el catalogo, listas para la pantalla. */
    val orderItems: List<OrderItem> get() = orderItems(orderLines, products)

    /** Total del pedido en centavos. */
    val orderTotalCents: Int get() = orderTotalCents(orderLines, products)

    val isOrderEmpty: Boolean get() = orderLines.isEmpty()

    fun isFavorite(productId: String): Boolean = productId in favoriteIds

    fun productById(productId: String): Coffee? = products.firstOrNull { it.id == productId }

    fun roasterById(roasterId: String): Roaster? = roasters.firstOrNull { it.id == roasterId }

    /** Unidades de un producto ya presentes en el pedido, para mostrar "N en el pedido". */
    fun orderQuantityOf(productId: String): Int = quantityOf(orderLines, productId)
}
