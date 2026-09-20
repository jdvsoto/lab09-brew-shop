package gt.uvg.brewshop.model

/**
 * Un cafe del catalogo.
 *
 * [roasterId] enlaza el producto con el tostador que lo produce. El cafe no guarda si
 * esta marcado como favorito: esa coleccion vive en el estado de la tienda, para no
 * tener dos copias del mismo dato.
 */
data class Coffee(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val roasterId: String,
    val origin: String,
    val altitude: String,
    val process: String,
    val roastLevel: String,
    val tastingNotes: String
)
