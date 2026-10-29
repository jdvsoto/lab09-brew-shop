package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.CatalogSortOrder
import gt.uvg.brewshop.model.Coffee

/**
 * Ordena el catalogo segun la preferencia. Kotlin puro, sin Compose ni Android.
 *
 * El catalogo generado tiene nombres y precios repetidos. El desempate por id hace que los
 * productos empatados queden siempre en el mismo orden, en cada apertura de la app.
 */
fun sortProducts(products: List<Coffee>, order: CatalogSortOrder): List<Coffee> =
    when (order) {
        CatalogSortOrder.NAME -> products.sortedWith(
            compareBy<Coffee, String>(String.CASE_INSENSITIVE_ORDER) { it.name }.thenBy { it.id }
        )

        CatalogSortOrder.PRICE -> products.sortedWith(
            compareBy<Coffee> { it.priceCents }.thenBy { it.id }
        )
    }
