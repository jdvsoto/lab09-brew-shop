package gt.uvg.brewshop.ui.store

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.brewshop.data.FavoriteEntity
import gt.uvg.brewshop.data.StoreDatabase
import gt.uvg.brewshop.data.observeCatalogSortOrder
import gt.uvg.brewshop.data.saveCatalogSortOrder
import gt.uvg.brewshop.data.storePreferencesDataStore
import gt.uvg.brewshop.data.toEntity
import gt.uvg.brewshop.data.toOrderLine
import gt.uvg.brewshop.domain.OrderResult
import gt.uvg.brewshop.domain.buildCatalog
import gt.uvg.brewshop.domain.createOrderReceipt
import gt.uvg.brewshop.domain.filterProductsByName
import gt.uvg.brewshop.domain.imageUrlFor
import gt.uvg.brewshop.domain.sortProducts
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.CatalogSortOrder
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.Roaster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import gt.uvg.brewshop.domain.addToOrder as applyAddToOrder
import gt.uvg.brewshop.domain.decreaseOrderLine as applyDecreaseOrderLine

/** El estado temporal no duplica los favoritos ni las lineas que Room conserva. */
private data class StoreMemoryState(
    val query: String = "",
    val message: String? = null,
    val lastReceipt: OrderReceipt? = null
)

/**
 * Unica fuente de verdad de la tienda. Room observa los favoritos y el pedido; DataStore
 * observa la preferencia. Solo busqueda, mensajes, recibo y checkout viven en memoria.
 * Las reglas del pedido siguen delegadas al dominio Kotlin puro.
 */
class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StoreDatabase.getInstance(application)
    private val favoriteDao = database.favoriteDao()
    private val orderLineDao = database.orderLineDao()
    private val preferences = application.storePreferencesDataStore

    // No se persiste el catalogo: la semilla fija reproduce los mismos productos e IDs.
    private val catalog = buildCatalog(originalCoffees, initialRoasters)
    private val memoryState = MutableStateFlow(StoreMemoryState())
    private val writeMutex = Mutex()

    val uiState: StateFlow<StoreUiState> = combine(
        memoryState,
        favoriteDao.observeFavoriteIds(),
        orderLineDao.observeOrderLines(),
        preferences.observeCatalogSortOrder()
    ) { memory, favoriteIds, orderLines, sortOrder ->
        StoreUiState(
            products = catalog,
            roasters = initialRoasters,
            favoriteIds = favoriteIds.toSet(),
            query = memory.query,
            visibleProducts = sortProducts(filterProductsByName(catalog, memory.query), sortOrder),
            orderLines = orderLines.map { it.toOrderLine() },
            message = memory.message,
            lastReceipt = memory.lastReceipt,
            sortOrder = sortOrder,
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(
            products = catalog,
            roasters = initialRoasters,
            visibleProducts = catalog,
            isLoaded = false
        )
    )

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()

    // Mantiene uiState suscrito para que el estado consultado al confirmar se actualice.
    val orderUnits: StateFlow<Int> = uiState
        .map { it.orderUnitCount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Vive fuera del formulario: reiniciarlo no altera la numeracion de la sesion.
    private var confirmedOrderCount = 0

    fun onQueryChange(query: String) {
        memoryState.update { it.copy(query = query) }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    /** Se valida el estado mas reciente de Room antes de escribir una cantidad. */
    fun addToOrder(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            writeMutex.withLock {
                val currentLines = orderLineDao.getOrderLines().map { it.toOrderLine() }
                when (val result = applyAddToOrder(currentLines, catalog, productId, quantity)) {
                    is OrderResult.Success -> {
                        result.lines.firstOrNull { it.productId == productId }?.let { line ->
                            orderLineDao.upsert(line.toEntity())
                        }
                        memoryState.update { it.copy(message = result.message) }
                    }

                    is OrderResult.Rejected -> {
                        // La operacion rechazada no modifica ninguna fila del pedido.
                        memoryState.update { it.copy(message = result.reason) }
                    }
                }
            }
        }
    }

    fun increaseOrderLine(productId: String) {
        addToOrder(productId, quantity = 1)
    }

    /** Al bajar de una unidad a cero, la fila se elimina tambien del disco. */
    fun decreaseOrderLine(productId: String) {
        viewModelScope.launch {
            writeMutex.withLock {
                val currentLines = orderLineDao.getOrderLines().map { it.toOrderLine() }
                val updatedLines = applyDecreaseOrderLine(currentLines, productId)
                val updated = updatedLines.firstOrNull { it.productId == productId }
                when {
                    updated != null -> orderLineDao.upsert(updated.toEntity())
                    currentLines.any { it.productId == productId } -> orderLineDao.delete(productId)
                }
            }
        }
    }

    fun removeOrderLine(productId: String) {
        viewModelScope.launch {
            writeMutex.withLock {
                orderLineDao.delete(productId)
            }
        }
    }

    fun consumeMessage() {
        memoryState.update { it.copy(message = null) }
    }

    /** Consulta la tabla dentro del Mutex para evitar toques consecutivos inconsistentes. */
    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            writeMutex.withLock {
                if (favoriteDao.isFavorite(productId)) {
                    favoriteDao.delete(productId)
                } else {
                    favoriteDao.insert(FavoriteEntity(productId))
                }
            }
        }
    }

    fun onCatalogSortOrderChange(order: CatalogSortOrder) {
        viewModelScope.launch {
            preferences.saveCatalogSortOrder(order)
        }
    }

    fun onFullNameChange(value: String) {
        _checkoutUiState.update { it.copy(fullName = it.fullName.edited(value)) }
    }

    fun onPhoneChange(value: String) {
        _checkoutUiState.update { it.copy(phone = it.phone.edited(value)) }
    }

    fun onNitChange(value: String) {
        _checkoutUiState.update { it.copy(nit = it.nit.edited(value)) }
    }

    fun onBusinessNameChange(value: String) {
        _checkoutUiState.update { it.copy(businessName = it.businessName.edited(value)) }
    }

    fun onPaymentMethodChange(paymentMethod: PaymentMethod) {
        _checkoutUiState.update { it.copy(paymentMethod = paymentMethod) }
    }

    /**
     * Al pasar a CF se reinician los indicadores de interaccion del NIT y la razon social.
     * Los errores se derivan del formulario y no hay errores almacenados que limpiar.
     */
    fun onBillingTypeChange(billingType: BillingType) {
        _checkoutUiState.update { current ->
            when (billingType) {
                BillingType.CF -> current.copy(
                    billingType = billingType,
                    nit = current.nit.copy(isTouched = false),
                    businessName = current.businessName.copy(isTouched = false)
                )

                BillingType.NIT -> current.copy(billingType = billingType)
            }
        }
    }

    /**
     * El recibo se calcula con el total anterior al vaciado. Un formulario invalido o un
     * pedido vacio no altera ni el recibo ni las tablas. Los favoritos quedan intactos.
     */
    fun confirmOrder(): Boolean {
        val form = _checkoutUiState.value
        val store = uiState.value
        if (!form.isFormValid || store.orderUnitCount == 0) return false

        confirmedOrderCount += 1
        val receipt = createOrderReceipt(
            orderNumber = confirmedOrderCount,
            customerName = form.fullName.value,
            phone = form.phone.value,
            billingType = form.billingType,
            nit = form.nit.value,
            businessName = form.businessName.value,
            paymentMethod = form.paymentMethod,
            unitCount = store.orderUnitCount,
            totalCents = store.orderTotalCents
        )

        memoryState.update { it.copy(lastReceipt = receipt, message = null) }
        _checkoutUiState.value = CheckoutUiState()
        viewModelScope.launch {
            writeMutex.withLock {
                orderLineDao.deleteAll()
            }
        }
        return true
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
