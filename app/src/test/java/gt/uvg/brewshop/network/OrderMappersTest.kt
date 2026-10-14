package gt.uvg.brewshop.network

import gt.uvg.brewshop.domain.OrderItem
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderMappersTest {

    private fun coffee(id: String, name: String, priceCents: Int) = Coffee(
        id = id,
        name = name,
        description = "",
        priceCents = priceCents,
        stock = 10,
        imageUrl = "",
        roasterId = "r1",
        origin = "",
        altitude = "",
        process = "",
        roastLevel = "",
        tastingNotes = ""
    )

    private val items = listOf(
        OrderItem(coffee("c2", "Geisha Huehuetenango", 14500), quantity = 2, subtotalCents = 29000),
        OrderItem(coffee("c1", "Bourbon Antigua", 7200), quantity = 1, subtotalCents = 7200)
    )

    @Test
    fun buildNewOrderDto_copiesNameAndPriceOfEachLine() {
        val dto = buildNewOrderDto(items, BillingType.CF, PaymentMethod.CASH_ON_DELIVERY, 1791907200000L)

        assertEquals(
            listOf(
                OrderLineDto("c2", "Geisha Huehuetenango", 14500, 2, 29000),
                OrderLineDto("c1", "Bourbon Antigua", 7200, 1, 7200)
            ),
            dto.lines
        )
        assertEquals(1791907200000L, dto.createdAt)
    }

    @Test
    fun buildNewOrderDto_sumsUnitsAndTotal() {
        val dto = buildNewOrderDto(items, BillingType.CF, PaymentMethod.CASH_ON_DELIVERY, 0L)

        assertEquals(3, dto.unitCount)
        assertEquals(36200, dto.totalCents)
    }

    @Test
    fun buildNewOrderDto_sendsBillingAndPaymentAsEnumNames() {
        val dto = buildNewOrderDto(items, BillingType.NIT, PaymentMethod.BANK_TRANSFER, 0L)

        assertEquals("NIT", dto.billingType)
        assertEquals("BANK_TRANSFER", dto.paymentMethod)
    }

    @Test
    fun toPlacedOrder_convertsLinesAndEnums() {
        val order = OrderDto(
            id = "7",
            createdAt = 1791907200000L,
            lines = listOf(OrderLineDto("c2", "Geisha Huehuetenango", 14500, 2, 29000)),
            unitCount = 2,
            totalCents = 29000,
            billingType = "NIT",
            paymentMethod = "BANK_TRANSFER"
        ).toPlacedOrder()

        assertEquals("7", order.id)
        assertEquals(1791907200000L, order.createdAt)
        assertEquals(1, order.lines.size)
        assertEquals("Geisha Huehuetenango", order.lines[0].name)
        assertEquals(14500, order.lines[0].unitPriceCents)
        assertEquals(2, order.lines[0].quantity)
        assertEquals(29000, order.lines[0].subtotalCents)
        assertEquals(2, order.unitCount)
        assertEquals(29000, order.totalCents)
        assertEquals(BillingType.NIT, order.billingType)
        assertEquals(PaymentMethod.BANK_TRANSFER, order.paymentMethod)
    }

    @Test
    fun toPlacedOrder_leavesUnknownEnumValuesAsNull() {
        val order = OrderDto(
            id = "8",
            createdAt = 0L,
            lines = emptyList(),
            unitCount = 0,
            totalCents = 0,
            billingType = "OTRO",
            paymentMethod = "BITCOIN"
        ).toPlacedOrder()

        assertNull(order.billingType)
        assertNull(order.paymentMethod)
    }
}
