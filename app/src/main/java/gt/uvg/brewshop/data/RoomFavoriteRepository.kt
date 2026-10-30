package gt.uvg.brewshop.data

import android.database.SQLException
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomFavoriteRepository(private val favoriteDao: FavoriteDao) : FavoriteRepository {

    // Consultar y escribir dentro del mismo candado evita que dos toques seguidos se pisen.
    private val mutex = Mutex()

    override fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeFavoriteIds().map { it.toSet() }

    override suspend fun toggleFavorite(productId: String): Result<Unit> = mutex.withLock {
        try {
            if (favoriteDao.isFavorite(productId)) {
                favoriteDao.delete(productId)
            } else {
                favoriteDao.insert(FavoriteEntity(productId))
            }
            Result.success(Unit)
        } catch (e: SQLException) {
            Result.failure(DataException(DataErrorReason.STORAGE, e))
        }
    }
}
