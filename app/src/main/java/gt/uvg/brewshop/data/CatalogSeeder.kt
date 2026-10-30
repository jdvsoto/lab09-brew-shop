package gt.uvg.brewshop.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import gt.uvg.brewshop.domain.buildCatalog
import gt.uvg.brewshop.domain.originalCoffees
import gt.uvg.brewshop.domain.storeRoasters
import gt.uvg.brewshop.model.Coffee
import kotlinx.coroutines.tasks.await

private const val SEED_BATCH_SIZE = 100

/**
 * TEMPORAL: carga unica de los 500 productos del laboratorio 10 en Firestore. Se elimina
 * despues de ejecutarla una vez, junto con su boton.
 */
class CatalogSeeder(private val firestore: FirebaseFirestore) {

    /** Devuelve cuantos productos se guardaron. El id del documento es el id del producto. */
    suspend fun seed(): Result<Int> {
        val catalog = buildCatalog(originalCoffees, storeRoasters)
        val products = firestore.collection(PRODUCTS_COLLECTION)
        return try {
            catalog.chunked(SEED_BATCH_SIZE).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { product ->
                    batch.set(products.document(product.id), product.toDocumentData())
                }
                batch.commit().await()
            }
            Result.success(catalog.size)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e.toDataException())
        }
    }

    /** Precio en centavos, como en el laboratorio 10, y existencias como enteros. */
    private fun Coffee.toDocumentData(): Map<String, Any> = mapOf(
        ProductFields.NAME to name,
        ProductFields.DESCRIPTION to description,
        ProductFields.PRICE_CENTS to priceCents,
        ProductFields.STOCK to stock,
        ProductFields.IMAGE_URL to imageUrl,
        ProductFields.ROASTER_ID to roasterId,
        ProductFields.ORIGIN to origin,
        ProductFields.ALTITUDE to altitude,
        ProductFields.PROCESS to process,
        ProductFields.ROAST_LEVEL to roastLevel,
        ProductFields.TASTING_NOTES to tastingNotes
    )
}
