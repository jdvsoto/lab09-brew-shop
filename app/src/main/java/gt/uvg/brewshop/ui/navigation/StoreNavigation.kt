package gt.uvg.brewshop.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import gt.uvg.brewshop.ui.screens.catalog.CatalogScreen
import gt.uvg.brewshop.ui.screens.detail.CoffeeDetailScreen
import gt.uvg.brewshop.ui.screens.roaster.RoasterProfileScreen
import gt.uvg.brewshop.ui.store.StoreViewModel
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoreNavKey : NavKey {
    @Serializable
    data object Catalog : StoreNavKey

    @Serializable
    data class CoffeeDetail(val coffeeId: String) : StoreNavKey

    @Serializable
    data class RoasterProfile(val roasterId: String) : StoreNavKey
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

    // Reinicia la posicion solo cuando la consulta cambia de verdad (nueva busqueda o
    // "Limpiar busqueda"), no cada vez que el catalogo vuelve a componerse al regresar
    // del detalle, porque esta corrutina vive en este composable persistente y no en la
    // pantalla del catalogo.
    LaunchedEffect(uiState.query) {
        gridState.scrollToItem(0)
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<StoreNavKey.Catalog> {
                CatalogScreen(
                    products = uiState.visibleProducts,
                    query = uiState.query,
                    resultCount = uiState.resultCount,
                    catalogSize = uiState.catalogSize,
                    hasNoResults = uiState.hasNoResults,
                    favoriteIds = uiState.favoriteIds,
                    gridState = gridState,
                    onQueryChange = storeViewModel::onQueryChange,
                    onClearQuery = storeViewModel::clearQuery,
                    onOpenCoffee = { coffeeId ->
                        backStack.add(StoreNavKey.CoffeeDetail(coffeeId))
                    },
                    onToggleFavorite = storeViewModel::toggleFavorite
                )
            }
            entry<StoreNavKey.CoffeeDetail> { key ->
                val coffee = uiState.productById(key.coffeeId)
                val roaster = coffee?.let { uiState.roasterById(it.roasterId) }

                if (coffee != null && roaster != null) {
                    CoffeeDetailScreen(
                        coffee = coffee,
                        roasterName = roaster.name,
                        isFavorite = uiState.isFavorite(coffee.id),
                        onToggleFavorite = { storeViewModel.toggleFavorite(coffee.id) },
                        onOpenRoaster = {
                            backStack.add(StoreNavKey.RoasterProfile(roaster.id))
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                } else {
                    Text("Café no encontrado")
                }
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