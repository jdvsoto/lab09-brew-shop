package gt.uvg.brewshop.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import gt.uvg.brewshop.model.CatalogSortOrder
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import java.io.IOException
import kotlinx.coroutines.flow.Flow

class DataStoreCatalogPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : CatalogPreferencesRepository {

    override fun observeSortOrder(): Flow<CatalogSortOrder> = dataStore.observeCatalogSortOrder()

    override suspend fun setSortOrder(order: CatalogSortOrder): Result<Unit> = try {
        dataStore.saveCatalogSortOrder(order)
        Result.success(Unit)
    } catch (e: IOException) {
        Result.failure(DataException(DataErrorReason.STORAGE, e))
    }
}
