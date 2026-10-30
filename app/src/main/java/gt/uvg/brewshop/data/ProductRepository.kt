package gt.uvg.brewshop.data

import gt.uvg.brewshop.model.Coffee
import kotlinx.coroutines.flow.Flow

/** Catalogo compartido. Las existencias que entrega son las que guarda el servidor. */
interface ProductRepository {

    /**
     * Catalogo en tiempo real: vuelve a emitir cada vez que cambia un producto en el
     * servidor. Termina con un DataException si no se puede leer.
     */
    fun observeProducts(): Flow<List<Coffee>>

    /**
     * Reserva una unidad de [productId]. Solo descuenta si, en el servidor y en ese momento,
     * quedan existencias; si no, falla con [gt.uvg.brewshop.model.DataErrorReason.OUT_OF_STOCK].
     */
    suspend fun reserveUnit(productId: String): Result<Unit>

    /** Devuelve [quantity] unidades reservadas a las existencias de [productId]. */
    suspend fun releaseUnits(productId: String, quantity: Int): Result<Unit>
}
