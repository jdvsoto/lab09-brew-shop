package gt.uvg.brewshop.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckoutValidatorsTest {

    @Test
    fun fullNameAcceptsAccentsEnieAndSurroundingSpaces() {
        assertNull(validateFullName("Ana"))
        assertNull(validateFullName("Ñoño"))
        assertNull(validateFullName("José"))
        assertNull(validateFullName("  María Morales  "))
    }

    @Test
    fun fullNameRejectsEmptyDigitsAndTooFewLetters() {
        assertEquals("Ingresa tu nombre completo.", validateFullName(""))
        assertEquals("Ingresa tu nombre completo.", validateFullName("   "))
        assertEquals("El nombre no puede contener números.", validateFullName("Ana2"))
        assertEquals("Ingresa al menos 3 letras.", validateFullName("Jo"))
        // Los espacios y signos no cuentan como letras.
        assertEquals("Ingresa al menos 3 letras.", validateFullName("A B"))
        assertEquals("Ingresa al menos 3 letras.", validateFullName("!!!"))
    }

    @Test
    fun phoneAcceptsExactlyEightDigits() {
        assertNull(validatePhoneNumber("55123456"))
        assertNull(validatePhoneNumber(" 55123456 "))
    }

    @Test
    fun phoneRejectsWrongLengthSpacesDashesAndPrefix() {
        val formatError = "Ingresa exactamente 8 dígitos, sin espacios ni guiones."
        assertEquals("Ingresa tu teléfono.", validatePhoneNumber(""))
        assertEquals(formatError, validatePhoneNumber("5512345"))
        assertEquals(formatError, validatePhoneNumber("551234567"))
        assertEquals(formatError, validatePhoneNumber("5512 3456"))
        assertEquals(formatError, validatePhoneNumber("5512-3456"))
        assertEquals(formatError, validatePhoneNumber("+50255123456"))
    }

    @Test
    fun nitAcceptsFiveOrMoreDigits() {
        assertNull(validateNit("12345"))
        assertNull(validateNit(" 1234567 "))
    }

    @Test
    fun nitRejectsEmptyNonDigitsAndShortValues() {
        assertEquals("Ingresa el NIT.", validateNit(""))
        assertEquals("El NIT solo puede tener dígitos.", validateNit("12a45"))
        assertEquals("Ingresa al menos 5 dígitos para este ejercicio.", validateNit("4512"))
    }

    @Test
    fun businessNameNeedsThreeCharactersAfterTrim() {
        assertNull(validateBusinessName("S.A"))
        assertNull(validateBusinessName("Guzmán Inversiones"))
        assertEquals("Ingresa al menos 3 caracteres.", validateBusinessName(""))
        assertEquals("Ingresa al menos 3 caracteres.", validateBusinessName("  ab  "))
    }
}
