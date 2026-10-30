package gt.uvg.brewshop.data

import android.database.SQLException
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import gt.uvg.brewshop.model.OrderLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCurrentOrderRepository(private val orderLineDao: OrderLineDao) : CurrentOrderRepository {

    override fun observeOrderLines(): Flow<List<OrderLine>> =
        orderLineDao.observeOrderLines().map { lines -> lines.map { it.toOrderLine() } }

    override suspend fun getOrderLines(): List<OrderLine> =
        orderLineDao.getOrderLines().map { it.toOrderLine() }

    override suspend fun saveLine(line: OrderLine): Result<Unit> = write {
        orderLineDao.upsert(line.toEntity())
    }

    override suspend fun removeLine(productId: String): Result<Unit> = write {
        orderLineDao.delete(productId)
    }

    override suspend fun clear(): Result<Unit> = write {
        orderLineDao.deleteAll()
    }

    private suspend fun write(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: SQLException) {
        Result.failure(DataException(DataErrorReason.STORAGE, e))
    }
}
