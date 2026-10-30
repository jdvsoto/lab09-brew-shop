package gt.uvg.brewshop.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.TransactionOptions
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Cuando muchas reservas del mismo producto coinciden, solo una gana cada ronda y las demas
 * se repiten con el valor nuevo. Con el limite por defecto (5) una rafaga podria agotar los
 * reintentos aunque todavia quedaran existencias.
 */
private const val MAX_TRANSACTION_ATTEMPTS = 30

/** Resultado de una transaccion de existencias. */
private enum class StockChange { APPLIED, OUT_OF_STOCK, MISSING_PRODUCT }

class FirestoreProductRepository(private val firestore: FirebaseFirestore) : ProductRepository {

    private val products = firestore.collection(PRODUCTS_COLLECTION)

    private val transactionOptions = TransactionOptions.Builder()
        .setMaxAttempts(MAX_TRANSACTION_ATTEMPTS)
        .build()

    override fun observeProducts(): Flow<List<Coffee>> = callbackFlow {
        val registration = products.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error.toDataException())
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            // Sin conexion y sin copia local, Firestore entrega una lista vacia desde la cache.
            // Se trata como error para no mostrar un catalogo vacio ni quedarse cargando.
            if (snapshot.isEmpty && snapshot.metadata.isFromCache) {
                close(DataException(DataErrorReason.NO_CONNECTION))
                return@addSnapshotListener
            }
            trySend(snapshot.documents.mapNotNull { it.toCoffee() })
        }
        awaitClose { registration.remove() }
    }

    override suspend fun reserveUnit(productId: String): Result<Unit> =
        changeStock(productId, delta = -1)

    override suspend fun releaseUnits(productId: String, quantity: Int): Result<Unit> =
        changeStock(productId, delta = quantity)

    /**
     * Cambia las existencias dentro de una transaccion: lee el valor guardado en el servidor y
     * escribe el nuevo solo si nadie lo modifico entre la lectura y la escritura. Si otra app
     * lo cambio, Firestore descarta el intento y repite la funcion con el valor actualizado,
     * asi que dos reservas nunca descuentan la misma unidad. Las transacciones necesitan
     * conexion: sin red fallan de inmediato en lugar de quedar en cola.
     */
    private suspend fun changeStock(productId: String, delta: Int): Result<Unit> {
        val document = products.document(productId)
        return try {
            val change = firestore.runTransaction(transactionOptions) { transaction ->
                val stock = transaction.get(document).getLong(ProductFields.STOCK)
                when {
                    stock == null -> StockChange.MISSING_PRODUCT
                    stock + delta < 0 -> StockChange.OUT_OF_STOCK
                    else -> {
                        transaction.update(document, ProductFields.STOCK, stock + delta)
                        StockChange.APPLIED
                    }
                }
            }.await()
            when (change) {
                StockChange.APPLIED -> Result.success(Unit)
                StockChange.OUT_OF_STOCK -> Result.failure(DataException(DataErrorReason.OUT_OF_STOCK))
                StockChange.MISSING_PRODUCT -> Result.failure(DataException(DataErrorReason.NOT_FOUND))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e.toDataException())
        }
    }
}
