package gt.uvg.brewshop.ui.store

import androidx.lifecycle.ViewModel
import gt.uvg.brewshop.domain.OrderResult
import gt.uvg.brewshop.domain.buildCatalog
import gt.uvg.brewshop.domain.filterProductsByName
import gt.uvg.brewshop.domain.imageUrlFor
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.Roaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import gt.uvg.brewshop.domain.addToOrder as applyAddToOrder
import gt.uvg.brewshop.domain.decreaseOrderLine as applyDecreaseOrderLine
import gt.uvg.brewshop.domain.removeOrderLine as applyRemoveOrderLine

/**
 * Unica fuente de verdad de la tienda.
 *
 * Todas las pantallas pertenecen a la misma feature y comparten catalogo, consulta,
 * favoritos y pedido, por lo que existe un solo ViewModel obtenido en la raiz.
 *
 * Este ViewModel no abre pantallas ni modifica la pila de navegacion, y no contiene las
 * reglas del pedido: las delega a las funciones de [gt.uvg.brewshop.domain], que son
 * Kotlin puro y pueden ejecutarse sin interfaz.
 */
class StoreViewModel : ViewModel() {

    // El catalogo se genera una sola vez por instancia del ViewModel. Como el ViewModel
    // sobrevive a la rotacion, los productos, sus precios y sus IDs no cambian.
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    /** Actualiza la consulta y recalcula una sola vez la lista visible. */
    fun onQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(
                query = query,
                visibleProducts = filterProductsByName(current.products, query)
            )
        }
    }

    /** Limpia la busqueda y recupera el catalogo completo en su orden original. */
    fun clearQuery() {
        onQueryChange("")
    }

    /**
     * Agrega unidades al pedido. Si la regla rechaza la operacion, el pedido se conserva
     * exactamente igual y solo cambia el mensaje visible.
     */
    fun addToOrder(productId: String, quantity: Int = 1) {
        _uiState.update { current ->
            when (val result = applyAddToOrder(current.orderLines, current.products, productId, quantity)) {
                is OrderResult.Success -> current.copy(
                    orderLines = result.lines,
                    message = result.message
                )

                is OrderResult.Rejected -> current.copy(message = result.reason)
            }
        }
    }

    /** Aumenta en una unidad desde el resumen del pedido, con la misma validacion. */
    fun increaseOrderLine(productId: String) {
        addToOrder(productId, quantity = 1)
    }

    /** Disminuye en una unidad. Llegar a cero elimina la linea. */
    fun decreaseOrderLine(productId: String) {
        _uiState.update { current ->
            current.copy(orderLines = applyDecreaseOrderLine(current.orderLines, productId))
        }
    }

    fun removeOrderLine(productId: String) {
        _uiState.update { current ->
            current.copy(orderLines = applyRemoveOrderLine(current.orderLines, productId))
        }
    }

    /** La UI llama a esto despues de mostrar un mensaje, para que no se repita. */
    fun consumeMessage() {
        _uiState.update { current -> current.copy(message = null) }
    }

    /** Favorito del laboratorio 09: produce un estado nuevo, sin mutar el actual. */
    fun toggleFavorite(productId: String) {
        _uiState.update { current ->
            val favorites = if (productId in current.favoriteIds) {
                current.favoriteIds - productId
            } else {
                current.favoriteIds + productId
            }
            current.copy(favoriteIds = favorites)
        }
    }
}

private fun buildInitialState(): StoreUiState {
    val products = buildCatalog(originalCoffees, initialRoasters)
    return StoreUiState(
        products = products,
        roasters = initialRoasters,
        visibleProducts = products
    )
}

private val initialRoasters = listOf(
    Roaster(
        id = "r1",
        name = "Tostaduria La Bendicion",
        role = "Tostador artesanal",
        location = "Antigua Guatemala, Sacatepequez",
        description = "Taller familiar de tercera generacion. Tuesta en lotes de ocho " +
            "kilos y deja descansar cada lote cinco dias antes de venderlo, para que los " +
            "azucares del grano terminen de asentarse."
    ),
    Roaster(
        id = "r2",
        name = "Xelaju Roasters",
        role = "Micro-tostador de altura",
        location = "Quetzaltenango",
        description = "Compra directamente a doce productores del altiplano y paga sobre " +
            "el precio de bolsa. Trabaja tuestes claros para que se note el origen de " +
            "cada finca en la taza."
    ),
    Roaster(
        id = "r3",
        name = "Cardamomo Cafe",
        role = "Tostador de origen unico",
        location = "Coban, Alta Verapaz",
        description = "Nacio dentro de una finca de cardamomo y hoy tuesta solo lotes de " +
            "las Verapaces. Publica la fecha de tueste de cada bolsa."
    ),
    Roaster(
        id = "r4",
        name = "Volcan Tostadores",
        role = "Tostador experimental",
        location = "Ciudad de Guatemala",
        description = "Trabaja procesos anaerobicos y fermentaciones controladas. Cada " +
            "lote lleva la ficha de su fermentacion en la etiqueta."
    ),
    Roaster(
        id = "r5",
        name = "Finca Los Cipreses",
        role = "Productor que tuesta su cosecha",
        location = "Acatenango, Chimaltenango",
        description = "Cultiva, despulpa y tuesta en la misma finca. Vende unicamente lo " +
            "que produce, asi que su catalogo cambia con cada cosecha."
    ),
    Roaster(
        id = "r6",
        name = "Ruta del Altiplano",
        role = "Tostador cooperativo",
        location = "San Marcos",
        description = "Reune a cuarenta pequenos caficultores de la boca costa. El " +
            "excedente de cada venta regresa a la cooperativa."
    )
)

/**
 * Productos originales del laboratorio 09. Conservan sus IDs y su relacion con el
 * tostador; solo se les agregaron existencias e imagen. Las existencias estan elegidas
 * para que el catalogo empiece con un caso agotado, uno de exactamente 3 unidades y dos
 * con mas, que son los casos que pide comprobar el pedido.
 */
private val originalCoffees = listOf(
    Coffee(
        id = "c1",
        name = "Bourbon Antigua",
        description = "Clasico de valle volcanico: cuerpo redondo y dulzor de panela.",
        priceCents = 7200,
        stock = 12,
        imageUrl = imageUrlFor("c1"),
        roasterId = "r1",
        origin = "Finca El Pilar, Antigua",
        altitude = "1,550 msnm",
        process = "Lavado",
        roastLevel = "Tueste medio",
        tastingNotes = "Panela, cacao y naranja madura"
    ),
    Coffee(
        id = "c2",
        name = "Geisha Huehuetenango",
        description = "Taza floral y delicada, la mas aromatica del catalogo.",
        priceCents = 14500,
        stock = 3,
        imageUrl = imageUrlFor("c2"),
        roasterId = "r2",
        origin = "La Libertad, Huehuetenango",
        altitude = "1,900 msnm",
        process = "Lavado",
        roastLevel = "Tueste claro",
        tastingNotes = "Jazmin, durazno y bergamota"
    ),
    Coffee(
        id = "c3",
        name = "Caturra Atitlan",
        description = "Equilibrado y cremoso, pensado para prensa francesa.",
        priceCents = 6800,
        stock = 0,
        imageUrl = imageUrlFor("c3"),
        roasterId = "r1",
        origin = "San Juan La Laguna, Solola",
        altitude = "1,650 msnm",
        process = "Honey",
        roastLevel = "Tueste medio",
        tastingNotes = "Almendra, miel y manzana roja"
    ),
    Coffee(
        id = "c4",
        name = "Pacamara Coban",
        description = "Grano grande de proceso natural, dulce y afrutado.",
        priceCents = 9800,
        stock = 7,
        imageUrl = imageUrlFor("c4"),
        roasterId = "r2",
        origin = "Coban, Alta Verapaz",
        altitude = "1,400 msnm",
        process = "Natural",
        roastLevel = "Tueste claro",
        tastingNotes = "Fresa, cacao con leche y vainilla"
    )
)
