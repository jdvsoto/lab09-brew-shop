package gt.uvg.brewshop.data

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderLineDao {

    /**
     * Lineas en el orden en que se agregaron. Upsert actualiza la fila en su lugar, asi que
     * subir una cantidad no mueve la linea al final.
     */
    @Query("SELECT * FROM order_lines ORDER BY rowid")
    fun observeOrderLines(): Flow<List<OrderLineEntity>>

    /** Lectura puntual para validar contra lo que hay en disco justo antes de escribir. */
    @Query("SELECT * FROM order_lines ORDER BY rowid")
    suspend fun getOrderLines(): List<OrderLineEntity>

    @Upsert
    suspend fun upsert(line: OrderLineEntity)

    @Query("DELETE FROM order_lines WHERE productId = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM order_lines")
    suspend fun deleteAll()
}
