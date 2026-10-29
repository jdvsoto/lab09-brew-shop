package gt.uvg.brewshop.model

/**
 * Orden del catalogo elegido por la persona.
 *
 * Se guarda [storageValue] y no el nombre del enum, para que renombrar una constante no
 * invalide lo que ya esta guardado en el telefono.
 */
enum class CatalogSortOrder(val storageValue: String) {
    NAME("name"),
    PRICE("price");

    companion object {
        /** Orden por defecto cuando no hay nada guardado o lo guardado no se reconoce. */
        fun fromStorage(value: String?): CatalogSortOrder =
            entries.firstOrNull { it.storageValue == value } ?: NAME
    }
}
