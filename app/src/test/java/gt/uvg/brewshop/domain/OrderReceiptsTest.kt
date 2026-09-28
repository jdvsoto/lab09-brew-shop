package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OrderReceiptsTest {

    @Test
    fun folioIsPaddedToFiveDigits() {
        assertEquals("#ORD-00001", formatOrderFolio(1))
        assertEquals("#ORD-00042", formatOrderFolio(42))
        assertEquals("#ORD-12345", formatOrderFolio(12345))
    }

    @Test
    fun consumerFinalReceiptDropsFiscalDataEvenIfTyped() {
        val receipt = createOrderReceipt(
            orderNumber = 1,
            customerName = "  Alberto Guzmán ",
            phone = "55442211",
            billingType = BillingType.CF,
            nit = "4512",
            businessName = "Guzmán Inversiones",
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            unitCount = 3,
            totalCents = 6750
        )

        assertEquals("#ORD-00001", receipt.folio)
        assertEquals("Alberto Guzmán", receipt.customerName)
        assertNull(receipt.nit)
        assertNull(receipt.businessName)
        assertEquals(6750, receipt.totalCents)
    }

    @Test
    fun nitReceiptKeepsTrimmedFiscalData() {
        val receipt = createOrderReceipt(
            orderNumber = 2,
            customerName = "Alberto Guzmán",
            phone = "55442211",
            billingType = BillingType.NIT,
            nit = " 1234567 ",
            businessName = "  Guzmán Inversiones S.A. ",
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            unitCount = 1,
            totalCents = 2250
        )

        assertEquals("#ORD-00002", receipt.folio)
        assertEquals("1234567", receipt.nit)
        assertEquals("Guzmán Inversiones S.A.", receipt.businessName)
    }
}
