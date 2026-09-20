package gt.uvg.brewshop.ui.store

import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.Roaster

/**
 * Estado inmutable que comparten las tres pantallas de la tienda.
 *
 * [favoriteIds] guarda unicamente identificadores. Asi saber si un cafe esta marcado es
 * una busqueda en un Set y no obliga a copiar el favorito dentro de cada producto ni
 * dentro de cada pantalla.
 */
data class StoreUiState(
    val coffees: List<Coffee> = emptyList(),
    val roasters: List<Roaster> = emptyList(),
    val favoriteIds: Set<String> = emptySet()
) {
    fun isFavorite(coffeeId: String): Boolean = coffeeId in favoriteIds

    fun coffeeById(coffeeId: String): Coffee? = coffees.firstOrNull { it.id == coffeeId }

    fun roasterById(roasterId: String): Roaster? = roasters.firstOrNull { it.id == roasterId }
}
