package gt.uvg.brewshop.ui.orders

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SPANISH = Locale.forLanguageTag("es-GT")
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", SPANISH)
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", SPANISH)

/**
 * Fecha de un pedido para mostrar: "hoy 10:41", "ayer 18:02" o "12 oct 2026, 18:02".
 * Recibe el momento actual y la zona para que el resultado sea el mismo en las pruebas.
 */
fun formatOrderDate(createdAtMillis: Long, nowMillis: Long, zone: ZoneId): String {
    val created = Instant.ofEpochMilli(createdAtMillis).atZone(zone)
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val createdDay = created.toLocalDate()

    return when (createdDay) {
        today -> "hoy ${created.format(TIME_FORMAT)}"
        today.minusDays(1) -> "ayer ${created.format(TIME_FORMAT)}"
        else -> created.format(DATE_FORMAT).replace(".", "")
    }
}
