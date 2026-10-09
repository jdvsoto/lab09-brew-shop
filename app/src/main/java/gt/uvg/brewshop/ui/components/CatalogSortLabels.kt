package gt.uvg.brewshop.ui.components

import gt.uvg.brewshop.model.CatalogSortOrder

/** Textos visibles del orden. Son presentacion, por eso viven en la UI y no en el modelo. */
fun CatalogSortOrder.displayName(): String = when (this) {
    CatalogSortOrder.NAME -> "Nombre"
    CatalogSortOrder.PRICE -> "Precio"
}
