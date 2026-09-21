package gt.uvg.brewshop.model

/**
 * Un cafe del catalogo.
 *
 * [roasterId] enlaza el producto con el tostador que lo produce. El cafe no guarda si
 * esta marcado como favorito ni cuantas unidades lleva el pedido: esas colecciones viven
 * en el estado de la tienda, para no tener dos copias del mismo dato.
 *
 * El precio se guarda en centavos enteros para que sumar lineas y calcular totales sea
 * exacto; formatear a "Q25.90" es responsabilidad de [gt.uvg.brewshop.domain.formatPriceCents].
 */
data class Coffee(
    val id: String,
    val name: String,
    val description: String,
    val priceCents: Int,
    val stock: Int,
    val imageUrl: String,
    val roasterId: String,
    val origin: String,
    val altitude: String,
    val process: String,
    val roastLevel: String,
    val tastingNotes: String
)
