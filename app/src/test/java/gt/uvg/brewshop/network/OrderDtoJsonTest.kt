package gt.uvg.brewshop.network

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderDtoJsonTest {

    // Respuesta real de GET /orders/2 en MockAPI, tal como la devolvio el servidor.
    private val realResponse =
        """{"createdAt":1791907200000,"lines":[{"productId":"c2","name":"Geisha Huehuetenango","unitPriceCents":14500,"quantity":2,"subtotalCents":29000},{"productId":"c1","name":"Bourbon Antigua","unitPriceCents":7200,"quantity":1,"subtotalCents":7200}],"unitCount":3,"totalCents":36200,"billingType":"CF","paymentMethod":"CASH_ON_DELIVERY","id":"2"}"""

    @Test
    fun decode_realServerResponse() {
        val order = StoreApi.json.decodeFromString<OrderDto>(realResponse)

        assertEquals("2", order.id)
        assertEquals(1791907200000L, order.createdAt)
        assertEquals(3, order.unitCount)
        assertEquals(36200, order.totalCents)
        assertEquals("CF", order.billingType)
        assertEquals("CASH_ON_DELIVERY", order.paymentMethod)
        assertEquals(
            listOf(
                OrderLineDto("c2", "Geisha Huehuetenango", 14500, 2, 29000),
                OrderLineDto("c1", "Bourbon Antigua", 7200, 1, 7200)
            ),
            order.lines
        )
    }

    @Test
    fun decode_ignoresFieldsTheDtoDoesNotDeclare() {
        val withExtraField = realResponse.replace("\"id\":\"2\"", "\"id\":\"2\",\"status\":\"new\"")

        val order = StoreApi.json.decodeFromString<OrderDto>(withExtraField)

        assertEquals("2", order.id)
        assertEquals(2, order.lines.size)
    }

    @Test(expected = SerializationException::class)
    fun decode_orderWithoutLines_throwsSerializationException() {
        val withoutLines = """{"createdAt":1791907200000,"unitCount":3,"totalCents":36200,"billingType":"CF","paymentMethod":"CASH_ON_DELIVERY","id":"2"}"""

        StoreApi.json.decodeFromString<OrderDto>(withoutLines)
    }

    @Test
    fun encode_newOrderDoesNotCarryAnId() {
        val newOrder = NewOrderDto(
            createdAt = 1791907200000L,
            lines = listOf(OrderLineDto("c1", "Bourbon Antigua", 7200, 1, 7200)),
            unitCount = 1,
            totalCents = 7200,
            billingType = "CF",
            paymentMethod = "CASH_ON_DELIVERY"
        )

        val json = StoreApi.json.encodeToString(NewOrderDto.serializer(), newOrder)

        assertEquals(false, json.contains("\"id\""))
    }
}
