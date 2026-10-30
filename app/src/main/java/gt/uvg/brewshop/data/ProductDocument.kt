package gt.uvg.brewshop.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException

internal const val PRODUCTS_COLLECTION = "products"

/** Nombres de los campos de un documento de la coleccion products. */
internal object ProductFields {
    const val NAME = "name"
    const val DESCRIPTION = "description"
    const val PRICE_CENTS = "priceCents"
    const val STOCK = "stock"
    const val IMAGE_URL = "imageUrl"
    const val ROASTER_ID = "roasterId"
    const val ORIGIN = "origin"
    const val ALTITUDE = "altitude"
    const val PROCESS = "process"
    const val ROAST_LEVEL = "roastLevel"
    const val TASTING_NOTES = "tastingNotes"
}

/**
 * Convierte un documento en el modelo de dominio. El id del documento es el id del producto,
 * asi que favoritos y pedido siguen apuntando a los mismos productos. Un documento al que le
 * falte un campo se descarta en lugar de cerrar la app.
 */
internal fun DocumentSnapshot.toCoffee(): Coffee? = Coffee(
    id = id,
    name = getString(ProductFields.NAME) ?: return null,
    description = getString(ProductFields.DESCRIPTION) ?: return null,
    priceCents = getLong(ProductFields.PRICE_CENTS)?.toInt() ?: return null,
    stock = getLong(ProductFields.STOCK)?.toInt() ?: return null,
    imageUrl = getString(ProductFields.IMAGE_URL) ?: return null,
    roasterId = getString(ProductFields.ROASTER_ID) ?: return null,
    origin = getString(ProductFields.ORIGIN) ?: return null,
    altitude = getString(ProductFields.ALTITUDE) ?: return null,
    process = getString(ProductFields.PROCESS) ?: return null,
    roastLevel = getString(ProductFields.ROAST_LEVEL) ?: return null,
    tastingNotes = getString(ProductFields.TASTING_NOTES) ?: return null
)

/** Traduce un fallo de Firestore a un motivo que la app sabe explicar. */
internal fun FirebaseFirestoreException.toDataException(): DataException {
    val reason = when (code) {
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> DataErrorReason.NO_CONNECTION
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DataErrorReason.NOT_SIGNED_IN
        FirebaseFirestoreException.Code.NOT_FOUND -> DataErrorReason.NOT_FOUND
        else -> DataErrorReason.SERVER
    }
    return DataException(reason, this)
}
