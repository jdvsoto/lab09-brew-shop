package gt.uvg.brewshop

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import gt.uvg.brewshop.data.CatalogPreferencesRepository
import gt.uvg.brewshop.data.CatalogSeeder
import gt.uvg.brewshop.data.CurrentOrderRepository
import gt.uvg.brewshop.data.DataStoreCatalogPreferencesRepository
import gt.uvg.brewshop.data.FavoriteRepository
import gt.uvg.brewshop.data.FirebaseSessionRepository
import gt.uvg.brewshop.data.FirestoreProductRepository
import gt.uvg.brewshop.data.MockApiPlacedOrderRepository
import gt.uvg.brewshop.data.PlacedOrderRepository
import gt.uvg.brewshop.data.ProductRepository
import gt.uvg.brewshop.data.RoomCurrentOrderRepository
import gt.uvg.brewshop.data.RoomFavoriteRepository
import gt.uvg.brewshop.data.SessionRepository
import gt.uvg.brewshop.data.StoreDatabase
import gt.uvg.brewshop.data.storePreferencesDataStore
import gt.uvg.brewshop.network.StoreApi

/**
 * Inyeccion manual: el unico lugar que conoce las implementaciones. Se crea una sola vez en
 * [BrewShopApplication] y cada ViewModel recibe aqui sus Repository por constructor.
 */
class AppContainer(context: Context) {

    private val database = StoreDatabase.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()

    val sessionRepository: SessionRepository = FirebaseSessionRepository(FirebaseAuth.getInstance())

    val productRepository: ProductRepository = FirestoreProductRepository(firestore)

    val favoriteRepository: FavoriteRepository = RoomFavoriteRepository(database.favoriteDao())

    val currentOrderRepository: CurrentOrderRepository =
        RoomCurrentOrderRepository(database.orderLineDao())

    val catalogPreferencesRepository: CatalogPreferencesRepository =
        DataStoreCatalogPreferencesRepository(context.storePreferencesDataStore)

    val placedOrderRepository: PlacedOrderRepository = MockApiPlacedOrderRepository(StoreApi.orderApi)

    // TEMPORAL: solo para la carga inicial del catalogo.
    val catalogSeeder = CatalogSeeder(firestore)
}
