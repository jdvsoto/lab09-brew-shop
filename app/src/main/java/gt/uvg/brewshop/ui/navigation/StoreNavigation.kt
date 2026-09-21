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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import gt.uvg.brewshop.ui.screens.detail.CoffeeDetailScreen
import gt.uvg.brewshop.ui.screens.order.OrderScreen
import gt.uvg.brewshop.ui.screens.roaster.RoasterProfileScreen
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
                    onBack = { backStack.removeLastOrNull() }
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
