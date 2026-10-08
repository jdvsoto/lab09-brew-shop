package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.CatalogSortOrder
import gt.uvg.brewshop.model.Coffee
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogSortingTest {

    private fun coffee(id: String, name: String, priceCents: Int) = Coffee(
        id = id,
        name = name,
        description = "",
        priceCents = priceCents,
        stock = 5,
        imageUrl = "",
        roasterId = "r1",
        origin = "",
        altitude = "",
        process = "",
        roastLevel = "",
        tastingNotes = ""
    )

    private val pacamara = coffee("c4", "Pacamara Coban", 9800)
    private val bourbon = coffee("c1", "Bourbon Antigua", 7200)
    private val caturra = coffee("c3", "caturra Atitlan", 6800)

    @Test
    fun nameOrderIgnoresCase() {
        val sorted = sortProducts(listOf(pacamara, caturra, bourbon), CatalogSortOrder.NAME)

        assertEquals(listOf("c1", "c3", "c4"), sorted.map { it.id })
    }

    @Test
    fun priceOrderGoesFromCheapestToMostExpensive() {
        val sorted = sortProducts(listOf(pacamara, bourbon, caturra), CatalogSortOrder.PRICE)

        assertEquals(listOf("c3", "c1", "c4"), sorted.map { it.id })
    }

    @Test
    fun tiesAreBrokenById() {
        val second = coffee("gen-0010", "Geisha Fraijanes 500 g", 31177)
        val first = coffee("gen-0007", "Geisha Fraijanes 500 g", 31177)

        assertEquals(
            listOf("gen-0007", "gen-0010"),
            sortProducts(listOf(second, first), CatalogSortOrder.NAME).map { it.id }
        )
        assertEquals(
            listOf("gen-0007", "gen-0010"),
            sortProducts(listOf(second, first), CatalogSortOrder.PRICE).map { it.id }
        )
    }

    @Test
    fun originalListIsNotModified() {
        val original = listOf(pacamara, bourbon, caturra)

        sortProducts(original, CatalogSortOrder.PRICE)

        assertEquals(listOf("c4", "c1", "c3"), original.map { it.id })
    }
}
