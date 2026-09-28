package gt.uvg.brewshop.domain

/*
 * Validadores del checkout. Cada uno recibe el texto tal como lo escribio la persona y
 * devuelve el mensaje de error, o null si el valor es valido. Son Kotlin puro: no conocen
 * Compose ni Android, y se prueban en la JVM.
 *
 * El recorte con trim() se hace aqui y no mientras se escribe, para no mover el cursor ni
 * borrar un espacio que la persona acaba de teclear.
 */

private const val MIN_NAME_LETTERS = 3
private const val PHONE_DIGITS = 8
private const val MIN_NIT_DIGITS = 5
private const val MIN_BUSINESS_NAME_LENGTH = 3

// Char.isDigit() tambien acepta digitos de otros alfabetos; telefono y NIT solo admiten 0-9.
private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

fun validateFullName(value: String): String? {
    val name = value.trim()
    return when {
        name.isEmpty() -> "Ingresa tu nombre completo."
        name.any { it.isDigit() } -> "El nombre no puede contener números."
        // isLetter() cuenta las letras con tilde y la ñ; espacios y signos no suman.
        name.count { it.isLetter() } < MIN_NAME_LETTERS -> "Ingresa al menos 3 letras."
        else -> null
    }
}

fun validatePhoneNumber(value: String): String? {
    val phone = value.trim()
    return when {
        phone.isEmpty() -> "Ingresa tu teléfono."
        phone.length != PHONE_DIGITS || !phone.all { it.isAsciiDigit() } ->
            "Ingresa exactamente 8 dígitos, sin espacios ni guiones."
        else -> null
    }
}

/** Regla del ejercicio: no comprueba el digito verificador ni consulta a SAT. */
fun validateNit(value: String): String? {
    val nit = value.trim()
    return when {
        nit.isEmpty() -> "Ingresa el NIT."
        !nit.all { it.isAsciiDigit() } -> "El NIT solo puede tener dígitos."
        nit.length < MIN_NIT_DIGITS -> "Ingresa al menos 5 dígitos para este ejercicio."
        else -> null
    }
}

fun validateBusinessName(value: String): String? =
    if (value.trim().length < MIN_BUSINESS_NAME_LENGTH) "Ingresa al menos 3 caracteres." else null
