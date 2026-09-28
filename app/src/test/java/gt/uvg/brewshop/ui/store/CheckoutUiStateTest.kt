package gt.uvg.brewshop.ui.store

import gt.uvg.brewshop.model.BillingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutUiStateTest {

    private val validConsumerFinal = CheckoutUiState(
        fullName = FormField("Alberto Guzmán", isTouched = true),
        phone = FormField("55442211", isTouched = true)
    )

    @Test
    fun initialStateIsInvalidButShowsNoErrors() {
        val state = CheckoutUiState()

        assertFalse(state.isFormValid)
        assertNull(state.visibleFullNameError)
        assertNull(state.visiblePhoneError)
        assertNull(state.visibleNitError)
        assertNull(state.visibleBusinessNameError)
    }

    @Test
    fun consumerFinalIgnoresInvalidNitText() {
        val state = validConsumerFinal.copy(nit = FormField("4512", isTouched = true))

        assertNull(state.nitError)
        assertNull(state.visibleNitError)
        assertTrue(state.isFormValid)
    }

    @Test
    fun switchingToNitBlocksWithHiddenErrorsUntilEdited() {
        val state = validConsumerFinal.copy(
            billingType = BillingType.NIT,
            nit = FormField("4512", isTouched = false)
        )

        assertFalse(state.isFormValid)
        assertEquals("Ingresa al menos 5 dígitos para este ejercicio.", state.nitError)
        assertNull(state.visibleNitError)
        assertNull(state.visibleBusinessNameError)
    }

    @Test
    fun validNitDataMakesTheFormValid() {
        val state = validConsumerFinal.copy(
            billingType = BillingType.NIT,
            nit = FormField("1234567", isTouched = true),
            businessName = FormField("Guzmán Inversiones", isTouched = true)
        )

        assertTrue(state.isFormValid)
    }

    @Test
    fun editedMarksTouchedOnlyWhenTextChanges() {
        val untouched = FormField("Ana")

        assertFalse(untouched.edited("Ana").isTouched)
        assertTrue(untouched.edited("Ana M").isTouched)
        assertTrue(untouched.edited("Ana M").edited("Ana M").isTouched)
    }
}
