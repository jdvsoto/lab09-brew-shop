package gt.uvg.brewshop.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogSortOrderTest {

    @Test
    fun storedValuesAreRecognized() {
        assertEquals(CatalogSortOrder.NAME, CatalogSortOrder.fromStorage("name"))
        assertEquals(CatalogSortOrder.PRICE, CatalogSortOrder.fromStorage("price"))
    }

    @Test
    fun missingOrUnknownValueFallsBackToName() {
        assertEquals(CatalogSortOrder.NAME, CatalogSortOrder.fromStorage(null))
        assertEquals(CatalogSortOrder.NAME, CatalogSortOrder.fromStorage("otra cosa"))
    }
}
