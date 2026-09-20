package gt.uvg.brewshop.ui.store

import androidx.lifecycle.ViewModel
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.Roaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Unica fuente de verdad de la tienda.
 *
 * Las tres pantallas pertenecen a la misma feature: catalogo y detalle comparten los
 * favoritos, y detalle y perfil consultan los mismos datos. Por eso existe un solo
 * ViewModel y no uno por pantalla.
 *
 * Este ViewModel no abre pantallas ni modifica la pila de navegacion.
 */
class StoreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        StoreUiState(
            coffees = initialCoffees,
            roasters = initialRoasters
        )
    )
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    /** Marca o desmarca un cafe produciendo un estado nuevo, sin mutar el actual. */
    fun toggleFavorite(coffeeId: String) {
        _uiState.update { current ->
            val favorites = if (coffeeId in current.favoriteIds) {
                current.favoriteIds - coffeeId
            } else {
                current.favoriteIds + coffeeId
            }
            current.copy(favoriteIds = favorites)
        }
    }
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
    )
)

private val initialCoffees = listOf(
    Coffee(
        id = "c1",
        name = "Bourbon Antigua",
        description = "Clasico de valle volcanico: cuerpo redondo y dulzor de panela.",
        price = 72.00,
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
        price = 145.00,
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
        price = 68.00,
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
        price = 98.00,
        roasterId = "r2",
        origin = "Coban, Alta Verapaz",
        altitude = "1,400 msnm",
        process = "Natural",
        roastLevel = "Tueste claro",
        tastingNotes = "Fresa, cacao con leche y vainilla"
    )
)
