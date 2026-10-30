package gt.uvg.brewshop.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gt.uvg.brewshop.BrewShopApplication
import gt.uvg.brewshop.data.CatalogPreferencesRepository
import gt.uvg.brewshop.data.CurrentOrderRepository
import gt.uvg.brewshop.data.FavoriteRepository
import gt.uvg.brewshop.data.PlacedOrderRepository
import gt.uvg.brewshop.data.ProductRepository
import gt.uvg.brewshop.data.SessionRepository
import gt.uvg.brewshop.domain.OrderResult
import gt.uvg.brewshop.domain.createOrderReceipt
import gt.uvg.brewshop.domain.filterProductsByName
import gt.uvg.brewshop.domain.sortProducts
import gt.uvg.brewshop.domain.storeRoasters
import gt.uvg.brewshop.model.BillingType
import gt.uvg.brewshop.model.CatalogSortOrder
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.OrderReceipt
import gt.uvg.brewshop.model.PaymentMethod
import gt.uvg.brewshop.model.dataErrorReason
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import gt.uvg.brewshop.domain.addToOrder as applyAddToOrder
import gt.uvg.brewshop.domain.decreaseOrderLine as applyDecreaseOrderLine

/** El estado temporal no duplica el catalogo, los favoritos ni las lineas de sus Repository. */
private data class StoreMemoryState(
    val query: String = "",
    val message: String? = null,
    val lastReceipt: OrderReceipt? = null,
    val reservingProductIds: Set<String> = emptySet(),
    val concurrencyTest: ConcurrencyTest? = null
)

/** Lo que entrega la lectura del catalogo: su estado y los productos, si ya llegaron. */
private data class CatalogLoad(
    val status: CatalogStatus,
    val products: List<Coffee> = emptyList()
)

/**
 * Unica fuente de verdad de la tienda. No sabe de donde vienen los datos: el catalogo, los
 * favoritos, el pedido en curso, la preferencia y los pedidos confirmados llegan de sus
 * Repository, que recibe por constructor. Las reglas del pedido siguen en el dominio.
 */
class StoreViewModel(
    private val sessionRepository: SessionRepository,
    private val productRepository: ProductRepository,
    private val favoriteRepository: FavoriteRepository,
    private val currentOrderRepository: CurrentOrderRepository,
    private val catalogPreferencesRepository: CatalogPreferencesRepository,
    private val placedOrderRepository: PlacedOrderRepository
) : ViewModel() {

    private val _memoryState = MutableStateFlow(StoreMemoryState())

    // Cada valor nuevo vuelve a abrir la lectura del catalogo: es el boton Reintentar.
    private val catalogRequests = MutableStateFlow(0)

    // Serializa los cambios del pedido: dos toques seguidos no leen la misma cantidad.
    private val orderMutex = Mutex()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val catalogLoad: Flow<CatalogLoad> = catalogRequests.flatMapLatest { loadCatalog() }

    val uiState: StateFlow<StoreUiState> = combine(
        _memoryState,
        catalogLoad,
        favoriteRepository.observeFavoriteIds(),
        currentOrderRepository.observeOrderLines(),
        catalogPreferencesRepository.observeSortOrder()
    ) { memory, catalog, favoriteIds, orderLines, sortOrder ->
        StoreUiState(
            products = catalog.products,
            roasters = storeRoasters,
            favoriteIds = favoriteIds,
            query = memory.query,
            // Buscar y ordenar se hacen en la app, sobre lo ya recibido: no se vuelve a leer.
            visibleProducts = sortProducts(filterProductsByName(catalog.products, memory.query), sortOrder),
            orderLines = orderLines,
            message = memory.message,
            lastReceipt = memory.lastReceipt,
            sortOrder = sortOrder,
            catalogStatus = catalog.status,
            reservingProductIds = memory.reservingProductIds,
            concurrencyTest = memory.concurrencyTest
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StoreUiState(roasters = storeRoasters)
    )

    private val _checkoutUiState = MutableStateFlow(CheckoutUiState())
    val checkoutUiState: StateFlow<CheckoutUiState> = _checkoutUiState.asStateFlow()

    // Mantiene uiState suscrito para que el estado consultado al confirmar se actualice.
    val orderUnits: StateFlow<Int> = uiState
        .map { it.orderUnitCount }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    // Vive fuera del formulario: reiniciarlo no altera la numeracion de la sesion.
    private var confirmedOrderCount = 0

    private val _submissionState = MutableStateFlow<OrderSubmissionState>(OrderSubmissionState.Idle)
    val submissionState: StateFlow<OrderSubmissionState> = _submissionState.asStateFlow()

    /**
     * Inicia la sesion anonima y despues escucha el catalogo. Un fallo deja el estado de error
     * en lugar de cerrar la app o quedarse cargando.
     */
    private fun loadCatalog(): Flow<CatalogLoad> = flow {
        emit(CatalogLoad(CatalogStatus.Loading))
        val sessionError = sessionRepository.signInAnonymously().exceptionOrNull()
        if (sessionError != null) {
            emit(CatalogLoad(CatalogStatus.Error(catalogErrorMessage(sessionError.dataErrorReason))))
            return@flow
        }
        emitAll(
            productRepository.observeProducts()
                .map { products -> CatalogLoad(CatalogStatus.Ready, products) }
                .catch { error ->
                    emit(CatalogLoad(CatalogStatus.Error(catalogErrorMessage(error.dataErrorReason))))
                }
        )
    }

    fun retryCatalog() {
        catalogRequests.update { it + 1 }
    }

    /** Actualiza la consulta y recalcula una sola vez la lista visible. */
    fun onQueryChange(query: String) {
        _memoryState.update { it.copy(query = query) }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    /**
     * Agrega una unidad reservandola primero en el servidor. Solo si la reserva se confirma se
     * guarda la linea; si falla, el pedido en curso no cambia.
     */
    fun addToOrder(productId: String) {
        changeReservation(productId) {
            val lines = currentOrderRepository.getOrderLines()
            when (val result = applyAddToOrder(lines, uiState.value.products, productId)) {
                is OrderResult.Rejected -> showMessage(result.reason)

                is OrderResult.Success -> {
                    val reservation = productRepository.reserveUnit(productId)
                    val reservationError = reservation.exceptionOrNull()
                    if (reservationError != null) {
                        showMessage(reservationErrorMessage(productId, reservationError.dataErrorReason))
                    } else {
                        val line = result.lines.first { it.productId == productId }
                        if (currentOrderRepository.saveLine(line).isSuccess) {
                            showMessage(result.message)
                        } else {
                            // La unidad ya se desconto: se devuelve para no dejarla reservada sin pedido.
                            productRepository.releaseUnits(productId, 1)
                            showMessage("No pudimos guardar tu pedido. Intenta de nuevo.")
                        }
                    }
                }
            }
        }
    }

    fun increaseOrderLine(productId: String) {
        addToOrder(productId)
    }

    /** Devuelve una unidad al servidor y solo entonces baja la cantidad; en cero, quita la linea. */
    fun decreaseOrderLine(productId: String) {
        changeReservation(productId) {
            val lines = currentOrderRepository.getOrderLines()
            if (lines.none { it.productId == productId }) return@changeReservation
            if (releaseOrExplain(productId, quantity = 1)) {
                val updated = applyDecreaseOrderLine(lines, productId).firstOrNull { it.productId == productId }
                if (updated != null) {
                    currentOrderRepository.saveLine(updated)
                } else {
                    currentOrderRepository.removeLine(productId)
                }
            }
        }
    }

    /** Devuelve todas las unidades de la linea al servidor y despues la elimina. */
    fun removeOrderLine(productId: String) {
        changeReservation(productId) {
            val line = currentOrderRepository.getOrderLines().firstOrNull { it.productId == productId }
                ?: return@changeReservation
            if (releaseOrExplain(productId, line.quantity)) {
                currentOrderRepository.removeLine(productId)
            }
        }
    }

    /**
     * Marca el producto como "reservando" mientras espera al servidor, para que la pantalla lo
     * indique y no acepte otro toque sobre el mismo producto. Los toques llegan en el hilo
     * principal, asi que revisar y marcar no compiten entre si.
     */
    private fun changeReservation(productId: String, block: suspend () -> Unit) {
        if (productId in _memoryState.value.reservingProductIds) return
        _memoryState.update { it.copy(reservingProductIds = it.reservingProductIds + productId) }
        viewModelScope.launch {
            try {
                orderMutex.withLock { block() }
            } finally {
                _memoryState.update { it.copy(reservingProductIds = it.reservingProductIds - productId) }
            }
        }
    }

    private suspend fun releaseOrExplain(productId: String, quantity: Int): Boolean {
        val error = productRepository.releaseUnits(productId, quantity).exceptionOrNull() ?: return true
        showMessage(
            if (error.dataErrorReason == DataErrorReason.NO_CONNECTION) {
                "Sin conexión no podemos devolver las unidades. El pedido no cambió."
            } else {
                "No pudimos devolver las unidades. El pedido no cambió."
            }
        )
        return false
    }

    /**
     * Herramienta de prueba: lanza [attempts] reservas a la vez, sin tocar el pedido en curso,
     * y cuenta cuantas acepto el servidor.
     */
    fun onConcurrencyTest(productId: String, attempts: Int) {
        val isRunning = _memoryState.value.concurrencyTest?.let { it.result == null } ?: false
        if (isRunning) return
        _memoryState.update { it.copy(concurrencyTest = ConcurrencyTest(productId)) }
        viewModelScope.launch {
            val results = List(attempts) {
                async { productRepository.reserveUnit(productId) }
            }.awaitAll()
            val successes = results.count { it.isSuccess }
            _memoryState.update {
                it.copy(
                    concurrencyTest = ConcurrencyTest(
                        productId = productId,
                        result = "Éxitos: $successes · Rechazos: ${attempts - successes}"
                    )
                )
            }
        }
    }

    fun consumeMessage() {
        _memoryState.update { it.copy(message = null) }
    }

    private fun showMessage(message: String) {
        _memoryState.update { it.copy(message = message) }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            favoriteRepository.toggleFavorite(productId).onFailure {
                showMessage("No pudimos guardar tu favorito.")
            }
        }
    }

    fun onCatalogSortOrderChange(order: CatalogSortOrder) {
        viewModelScope.launch {
            catalogPreferencesRepository.setSortOrder(order)
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
     * Inicia el envio del pedido al servidor y devuelve true si lo acepto. Vuelve a comprobar
     * el formulario y las unidades aunque el boton ya lo haga, porque deshabilitar un boton no
     * es validar. Reintentar es volver a llamarla: el pedido y el formulario siguen intactos.
     * Confirmar no cambia las existencias: las unidades ya se reservaron al agregarlas.
     */
    fun confirmOrder(): Boolean {
        // Evita el doble envio: mientras se espera al servidor, otro toque no hace nada.
        if (_submissionState.value is OrderSubmissionState.Sending) return false

        val form = _checkoutUiState.value
        val store = uiState.value
        if (!form.isFormValid || store.orderUnitCount == 0) return false

        _submissionState.value = OrderSubmissionState.Sending
        viewModelScope.launch {
            val result = placedOrderRepository.placeOrder(
                items = store.orderItems,
                billingType = form.billingType,
                paymentMethod = form.paymentMethod
            )
            _submissionState.value = result.fold(
                onSuccess = { orderId ->
                    // El pedido se vacia solo despues de que el servidor confirma que lo guardo.
                    completeConfirmedOrder(form, store)
                    OrderSubmissionState.Sent(orderId)
                },
                onFailure = { error -> OrderSubmissionState.Failed(submissionErrorMessage(error.dataErrorReason)) }
            )
        }
        return true
    }

    /**
     * Cierra la compra una vez que el servidor la guardo. Usa el formulario y el pedido
     * capturados antes de enviar, que son los datos que el servidor recibio.
     */
    private fun completeConfirmedOrder(form: CheckoutUiState, store: StoreUiState) {
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

        _memoryState.update { it.copy(lastReceipt = receipt, message = null) }
        _checkoutUiState.value = CheckoutUiState()
        viewModelScope.launch {
            orderMutex.withLock {
                currentOrderRepository.clear()
            }
        }
    }

    private fun reservationErrorMessage(productId: String, reason: DataErrorReason): String {
        val name = uiState.value.productById(productId)?.name ?: "El producto"
        return when (reason) {
            DataErrorReason.OUT_OF_STOCK -> "$name está agotado: otra persona reservó la última unidad. El pedido no cambió."
            DataErrorReason.NO_CONNECTION -> "Sin conexión no podemos reservar la unidad. El pedido no cambió."
            DataErrorReason.NOT_FOUND -> "$name ya no existe en el catálogo. El pedido no cambió."
            else -> "No pudimos reservar la unidad. El pedido no cambió."
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BrewShopApplication
                val container = application.container
                StoreViewModel(
                    sessionRepository = container.sessionRepository,
                    productRepository = container.productRepository,
                    favoriteRepository = container.favoriteRepository,
                    currentOrderRepository = container.currentOrderRepository,
                    catalogPreferencesRepository = container.catalogPreferencesRepository,
                    placedOrderRepository = container.placedOrderRepository
                )
            }
        }
    }
}

private fun catalogErrorMessage(reason: DataErrorReason): String = when (reason) {
    DataErrorReason.NO_CONNECTION -> "No pudimos cargar el catálogo. Revisa tu conexión."
    DataErrorReason.NOT_SIGNED_IN -> "No pudimos iniciar tu sesión para ver el catálogo."
    else -> "No pudimos cargar el catálogo. Intenta de nuevo."
}

private fun submissionErrorMessage(reason: DataErrorReason): String = when (reason) {
    DataErrorReason.NO_CONNECTION -> "No pudimos enviar tu pedido. Revisa tu conexión."
    DataErrorReason.UNEXPECTED_RESPONSE -> "Recibimos una respuesta inesperada del servidor."
    else -> "El servidor no pudo recibir tu pedido. Intenta de nuevo en un momento."
}
