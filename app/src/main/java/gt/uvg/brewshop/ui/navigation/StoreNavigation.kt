package gt.uvg.brewshop.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import gt.uvg.brewshop.ui.screens.catalog.CatalogScreen
import gt.uvg.brewshop.ui.screens.checkout.CheckoutScreen
import gt.uvg.brewshop.ui.screens.confirmation.OrderConfirmationScreen
import gt.uvg.brewshop.ui.screens.detail.CONCURRENCY_TEST_ATTEMPTS
import gt.uvg.brewshop.ui.screens.detail.CoffeeDetailScreen
import gt.uvg.brewshop.ui.orders.OrdersViewModel
import gt.uvg.brewshop.ui.screens.order.OrderScreen
import gt.uvg.brewshop.ui.screens.orders.OrderDetailScreen
import gt.uvg.brewshop.ui.screens.orders.OrdersScreen
import gt.uvg.brewshop.ui.screens.roaster.RoasterProfileScreen
import gt.uvg.brewshop.ui.store.CatalogStatus
import gt.uvg.brewshop.ui.store.OrderSubmissionState
import gt.uvg.brewshop.ui.store.StoreViewModel
import kotlinx.serialization.Serializable

/** Duracion de la transicion entre destinos, en milisegundos. */
private const val TRANSITION_MILLIS = 300

@Serializable
sealed interface StoreNavKey : NavKey {
    @Serializable
    data object Catalog : StoreNavKey

    @Serializable
    data class CoffeeDetail(val coffeeId: String) : StoreNavKey

    @Serializable
    data class RoasterProfile(val roasterId: String) : StoreNavKey

    @Serializable
    data object Order : StoreNavKey

    @Serializable
    data object Checkout : StoreNavKey

    @Serializable
    data object OrderConfirmation : StoreNavKey

    @Serializable
    data object Orders : StoreNavKey

    @Serializable
    data class OrderDetail(val orderId: String) : StoreNavKey
}

@Composable
fun StoreNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)
    // Los ViewModels reciben sus Repository por constructor, a traves de su Factory.
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModel.Factory)
    val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
    // "Mis pedidos" es otra feature, con su propio ViewModel obtenido tambien en la raiz.
    val ordersViewModel: OrdersViewModel = viewModel(factory = OrdersViewModel.Factory)

    // Cada carga remota responde al evento que abre su pantalla, no a su composicion: asi
    // rotar no repite la solicitud y un pedido recien enviado aparece al abrir la lista.
    val openOrders: () -> Unit = {
        ordersViewModel.loadOrders()
        backStack.add(StoreNavKey.Orders)
    }
    val backToCatalog: () -> Unit = {
        while (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    // El estado de scroll de la cuadricula es de la UI, no del ViewModel. Se crea aqui,
    // por encima de NavDisplay, para que sobreviva a ir al detalle y volver: la entrada
    // del catalogo puede recomponerse desde cero al regresar, pero este composable no.
    val gridState = rememberLazyGridState()

    // Clave de lo que la cuadricula muestra: busqueda y orden. Se guarda para distinguir un
    // cambio real de una composicion nueva, que es lo que ocurre al rotar.
    val catalogKey = "${uiState.query}|${uiState.sortOrder.storageValue}"
    var lastCatalogKey by rememberSaveable { mutableStateOf(catalogKey) }

    // Reinicia la posicion solo cuando la busqueda o el orden cambian de verdad, no al
    // regresar del detalle ni al rotar.
    LaunchedEffect(catalogKey) {
        if (catalogKey != lastCatalogKey) {
            lastCatalogKey = catalogKey
            gridState.scrollToItem(0)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        // Transiciones tomadas de la guia oficial de Navigation 3, "Animate between
        // destinations". Se adapto el ejemplo reduciendo la duracion a 300 ms y usando el
        // mismo par en el gesto predictivo, para que retroceder con el gesto y con el boton
        // se vean iguales. Avanzar entra desde la derecha; regresar invierte el sentido.
        // https://developer.android.com/guide/navigation/navigation-3/animate-destinations
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(TRANSITION_MILLIS)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(TRANSITION_MILLIS)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(TRANSITION_MILLIS)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_MILLIS)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(TRANSITION_MILLIS)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_MILLIS)
            )
        },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.visibleProducts,
                    query = uiState.query,
                    resultCount = uiState.resultCount,
                    catalogSize = uiState.catalogSize,
                    hasNoResults = uiState.hasNoResults,
                    favoriteIds = uiState.favoriteIds,
                    orderUnitCount = uiState.orderUnitCount,
                    gridState = gridState,
                    onQueryChange = storeViewModel::onQueryChange,
                    onClearQuery = storeViewModel::clearQuery,
                    onOpenCoffee = { coffeeId ->
                        backStack.add(StoreNavKey.CoffeeDetail(coffeeId))
                    },
                    onToggleFavorite = storeViewModel::toggleFavorite,
                    onOpenOrder = { backStack.add(StoreNavKey.Order) },
                    onOpenOrders = openOrders,
                    sortOrder = uiState.sortOrder,
                    catalogStatus = uiState.catalogStatus,
                    onSortOrderChange = storeViewModel::onCatalogSortOrderChange,
                    onRetryCatalog = storeViewModel::retryCatalog
                )
            }
            entry<StoreNavKey.CoffeeDetail> { key ->
                // Se resuelve por id contra el catalogo. Ambos accesos devuelven nullable,
                // asi que una clave que ya no exista muestra un aviso en lugar de fallar.
                val coffee = uiState.productById(key.coffeeId)
                val roaster = coffee?.let { uiState.roasterById(it.roasterId) }

                if (coffee != null && roaster != null) {
                    val concurrencyTest = uiState.concurrencyTestFor(coffee.id)
                    CoffeeDetailScreen(
                        coffee = coffee,
                        roasterName = roaster.name,
                        isFavorite = uiState.isFavorite(coffee.id),
                        quantityInOrder = uiState.orderQuantityOf(coffee.id),
                        canAddToOrder = uiState.canAddToOrder(coffee.id),
                        isReserving = uiState.isReserving(coffee.id),
                        orderUnitCount = uiState.orderUnitCount,
                        message = uiState.message,
                        onToggleFavorite = { storeViewModel.toggleFavorite(coffee.id) },
                        onAddToOrder = { storeViewModel.addToOrder(coffee.id) },
                        onMessageShown = storeViewModel::consumeMessage,
                        onOpenRoaster = {
                            backStack.add(StoreNavKey.RoasterProfile(roaster.id))
                        },
                        onOpenOrder = { backStack.add(StoreNavKey.Order) },
                        onBack = { backStack.removeLastOrNull() },
                        concurrencyTestResult = concurrencyTest?.result,
                        isConcurrencyTestRunning = concurrencyTest != null && concurrencyTest.result == null,
                        onConcurrencyTest = {
                            storeViewModel.onConcurrencyTest(coffee.id, CONCURRENCY_TEST_ATTEMPTS)
                        }
                    )
                } else if (uiState.catalogStatus is CatalogStatus.Loading) {
                    Text("Cargando…")
                } else {
                    Text("Café no encontrado")
                }
            }
            entry<StoreNavKey.Order> {
                OrderScreen(
                    items = uiState.orderItems,
                    totalCents = uiState.orderTotalCents,
                    message = uiState.message,
                    onIncrease = storeViewModel::increaseOrderLine,
                    onDecrease = storeViewModel::decreaseOrderLine,
                    onRemove = storeViewModel::removeOrderLine,
                    onMessageShown = storeViewModel::consumeMessage,
                    onBackToCatalog = {
                        // Deja solo el catalogo, que es la raiz de la pila.
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    onBack = { backStack.removeLastOrNull() },
                    canCheckout = uiState.canCheckout,
                    onContinueToCheckout = { backStack.add(StoreNavKey.Checkout) },
                    reservingProductIds = uiState.reservingProductIds
                )
            }
            entry<StoreNavKey.Checkout> {
                val checkoutState = storeViewModel.checkoutUiState.collectAsStateWithLifecycle()
                val orderUnitsState = storeViewModel.orderUnits.collectAsStateWithLifecycle()
                val submissionState = storeViewModel.submissionState.collectAsStateWithLifecycle()
                // Se recuerdan los objetos State y no sus valores: el estado derivado se crea
                // una sola vez y solo avisa cuando el resultado cambia.
                val isConfirmEnabled by remember(checkoutState, orderUnitsState, submissionState) {
                    derivedStateOf {
                        checkoutState.value.isFormValid &&
                            orderUnitsState.value > 0 &&
                            submissionState.value !is OrderSubmissionState.Sending
                    }
                }

                CheckoutScreen(
                    uiState = checkoutState.value,
                    orderItems = uiState.orderItems,
                    orderUnitCount = uiState.orderUnitCount,
                    totalCents = uiState.orderTotalCents,
                    isConfirmEnabled = isConfirmEnabled,
                    onFullNameChange = storeViewModel::onFullNameChange,
                    onPhoneChange = storeViewModel::onPhoneChange,
                    onBillingTypeChange = storeViewModel::onBillingTypeChange,
                    onNitChange = storeViewModel::onNitChange,
                    onBusinessNameChange = storeViewModel::onBusinessNameChange,
                    onPaymentMethodChange = storeViewModel::onPaymentMethodChange,
                    onConfirmOrder = {
                        // La confirmacion se abre de inmediato y muestra el progreso del envio.
                        // El checkout queda debajo para volver a el si el envio falla.
                        if (storeViewModel.confirmOrder()) {
                            backStack.add(StoreNavKey.OrderConfirmation)
                        }
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.OrderConfirmation> {
                val submission by storeViewModel.submissionState.collectAsStateWithLifecycle()

                // Mientras se espera al servidor no se sale: el resultado llegaria a una
                // pantalla que ya no esta visible.
                BackHandler(enabled = submission is OrderSubmissionState.Sending) { }
                // Tras el exito el pedido ya esta vacio: Atras lleva al catalogo, no al checkout.
                BackHandler(enabled = submission is OrderSubmissionState.Sent) { backToCatalog() }

                OrderConfirmationScreen(
                    submission = submission,
                    receipt = uiState.lastReceipt,
                    pendingTotalCents = uiState.orderTotalCents,
                    onRetry = { storeViewModel.confirmOrder() },
                    onBackToCheckout = { backStack.removeLastOrNull() },
                    // La pila queda [Catalog, Orders]: Atras desde la lista vuelve al catalogo.
                    onOpenOrders = {
                        backToCatalog()
                        openOrders()
                    },
                    onBackToCatalog = backToCatalog
                )
            }
            entry<StoreNavKey.Orders> {
                val ordersUiState by ordersViewModel.ordersUiState.collectAsStateWithLifecycle()

                OrdersScreen(
                    uiState = ordersUiState,
                    onOpenOrder = { orderId ->
                        ordersViewModel.loadOrderDetail(orderId)
                        backStack.add(StoreNavKey.OrderDetail(orderId))
                    },
                    onRetry = ordersViewModel::loadOrders,
                    onRefresh = ordersViewModel::loadOrders,
                    onGoToCatalog = backToCatalog,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StoreNavKey.OrderDetail> { key ->
                val detailUiState by ordersViewModel.orderDetailUiState.collectAsStateWithLifecycle()
                val backToOrders = {
                    backStack.removeLastOrNull()
                    // Se recarga para que un pedido borrado en el servidor deje de aparecer.
                    ordersViewModel.loadOrders()
                }

                BackHandler { backToOrders() }

                OrderDetailScreen(
                    uiState = detailUiState,
                    onRetry = { ordersViewModel.loadOrderDetail(key.orderId) },
                    onBackToOrders = backToOrders
                )
            }
            entry<StoreNavKey.RoasterProfile> { key ->
                val roaster = uiState.roasterById(key.roasterId)

                if (roaster != null) {
                    RoasterProfileScreen(
                        roaster = roaster,
                        onBack = { backStack.removeLastOrNull() }
                    )
                } else {
                    Text("Tostador no encontrado")
                }
            }
        }
    )
}
