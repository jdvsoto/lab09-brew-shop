package gt.uvg.brewshop.data

import gt.uvg.brewshop.model.OrderLine
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderLineEntityTest {

    @Test
    fun convertingToEntityAndBackKeepsTheLine() {
        val line = OrderLine(productId = "c2", quantity = 3)

        assertEquals(line, line.toEntity().toOrderLine())
        assertEquals(OrderLineEntity(productId = "c2", quantity = 3), line.toEntity())
    }
}
