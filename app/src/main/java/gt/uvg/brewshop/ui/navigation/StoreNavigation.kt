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
import gt.uvg.brewshop.ui.screens.detail.CoffeeDetailScreen
import gt.uvg.brewshop.ui.screens.order.OrderScreen
import gt.uvg.brewshop.ui.screens.roaster.RoasterProfileScreen
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
}

@Composable
fun StoreNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(StoreNavKey.Catalog)
    val storeViewModel: StoreViewModel = viewModel()
    val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()

    // El estado de scroll de la cuadricula es de la UI, no del ViewModel. Se crea aqui,
    // por encima de NavDisplay, para que sobreviva a ir al detalle y volver: la entrada
    // del catalogo puede recomponerse desde cero al regresar, pero este composable no.
    val gridState = rememberLazyGridState()

    // Consulta con la que se pinto la cuadricula por ultima vez. Se guarda para distinguir
    // un cambio real de busqueda de una composicion nueva con la misma consulta, que es lo
    // que ocurre al rotar: sin esta comparacion la rotacion mandaria la cuadricula al
    // inicio y se perderia la posicion.
    var lastQuery by rememberSaveable { mutableStateOf(uiState.query) }

    // Reinicia la posicion solo cuando la consulta cambia de verdad (nueva busqueda o
    // "Limpiar busqueda"), no al regresar del detalle ni al rotar.
    LaunchedEffect(uiState.query) {
        if (uiState.query != lastQuery) {
            lastQuery = uiState.query
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
                    onOpenOrder = { backStack.add(StoreNavKey.Order) }
                )
            }
            entry<StoreNavKey.CoffeeDetail> { key ->
                // Se resuelve por id contra el catalogo. Ambos accesos devuelven nullable,
                // asi que una clave que ya no exista muestra un aviso en lugar de fallar.
                val coffee = uiState.productById(key.coffeeId)
                val roaster = coffee?.let { uiState.roasterById(it.roasterId) }

                if (coffee != null && roaster != null) {
                    CoffeeDetailScreen(
                        coffee = coffee,
                        roasterName = roaster.name,
                        isFavorite = uiState.isFavorite(coffee.id),
                        quantityInOrder = uiState.orderQuantityOf(coffee.id),
                        canAddToOrder = uiState.canAddToOrder(coffee.id),
                        orderUnitCount = uiState.orderUnitCount,
                        message = uiState.message,
                        onToggleFavorite = { storeViewModel.toggleFavorite(coffee.id) },
                        onAddToOrder = { storeViewModel.addToOrder(coffee.id) },
                        onMessageShown = storeViewModel::consumeMessage,
                        onOpenRoaster = {
                            backStack.add(StoreNavKey.RoasterProfile(roaster.id))
                        },
                        onOpenOrder = { backStack.add(StoreNavKey.Order) },
                        onBack = { backStack.removeLastOrNull() }
                    )
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
                    onContinueToCheckout = { backStack.add(StoreNavKey.Checkout) }
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
                val backToCatalog = {
                    while (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }

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
                    // La Parte 3 lo conecta con "Mis pedidos".
                    onOpenOrders = backToCatalog,
                    onBackToCatalog = backToCatalog
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
