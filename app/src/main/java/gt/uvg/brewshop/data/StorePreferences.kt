package gt.uvg.brewshop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import gt.uvg.brewshop.model.CatalogSortOrder
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Una sola instancia por archivo: dos para el mismo nombre lanzan IllegalStateException.
val Context.storePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "store_preferences"
)

private val CATALOG_SORT_ORDER_KEY = stringPreferencesKey("catalog_sort_order")

fun DataStore<Preferences>.observeCatalogSortOrder(): Flow<CatalogSortOrder> =
    data
        .catch { error ->
            // Si el archivo no se puede leer se usa el valor por defecto en vez de cerrar la app.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> CatalogSortOrder.fromStorage(preferences[CATALOG_SORT_ORDER_KEY]) }

suspend fun DataStore<Preferences>.saveCatalogSortOrder(order: CatalogSortOrder) {
    edit { preferences -> preferences[CATALOG_SORT_ORDER_KEY] = order.storageValue }
}
