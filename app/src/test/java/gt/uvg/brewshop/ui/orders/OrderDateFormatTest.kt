package gt.uvg.brewshop.ui.orders

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderDateFormatTest {

    private val zone = ZoneId.of("America/Guatemala")
    private val now = ZonedDateTime.of(2026, 10, 13, 22, 0, 0, 0, zone).toInstant().toEpochMilli()

    private fun millis(day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(2026, 10, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun sameDayShowsToday() {
        assertEquals("hoy 10:41", formatOrderDate(millis(13, 10, 41), now, zone))
    }

    @Test
    fun previousDayShowsYesterday() {
        assertEquals("ayer 18:02", formatOrderDate(millis(12, 18, 2), now, zone))
    }

    @Test
    fun olderDaysShowTheFullDate() {
        assertEquals("10 oct 2026, 09:15", formatOrderDate(millis(10, 9, 15), now, zone))
    }

    @Test
    fun theDayIsDecidedInTheGivenZone() {
        // 23:30 en Guatemala ya es el dia siguiente en UTC: debe seguir contando como hoy.
        assertEquals("hoy 23:30", formatOrderDate(millis(13, 23, 30), now, zone))
    }
}
