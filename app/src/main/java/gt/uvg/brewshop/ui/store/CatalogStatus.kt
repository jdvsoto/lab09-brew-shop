package gt.uvg.brewshop.ui.store

/** Estado de la lectura del catalogo desde el servidor. */
sealed interface CatalogStatus {
    data object Loading : CatalogStatus

    data object Ready : CatalogStatus

    data class Error(val message: String) : CatalogStatus
}

/** Prueba de concurrencia de un producto. [result] es null mientras se ejecuta. */
data class ConcurrencyTest(
    val productId: String,
    val result: String? = null
)
