package gt.uvg.brewshop.data

import gt.uvg.brewshop.model.CatalogSortOrder
import kotlinx.coroutines.flow.Flow

/** Preferencias del catalogo guardadas en el dispositivo. */
interface CatalogPreferencesRepository {

    fun observeSortOrder(): Flow<CatalogSortOrder>

    suspend fun setSortOrder(order: CatalogSortOrder): Result<Unit>
}
