package gt.uvg.brewshop.data

import kotlinx.coroutines.flow.Flow

/** Favoritos guardados en el dispositivo. */
interface FavoriteRepository {

    fun observeFavoriteIds(): Flow<Set<String>>

    /** Marca el producto si no lo estaba y lo desmarca si ya lo estaba. */
    suspend fun toggleFavorite(productId: String): Result<Unit>
}
