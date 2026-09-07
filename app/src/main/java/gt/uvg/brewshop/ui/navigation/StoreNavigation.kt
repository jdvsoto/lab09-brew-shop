package gt.uvg.brewshop.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                    coffees = uiState.coffees,
                    favoriteIds = uiState.favoriteIds,
                    onOpenCoffee = { coffeeId ->
                        backStack.add(StoreNavKey.CoffeeDetail(coffeeId))
                    },
                    onToggleFavorite = storeViewModel::toggleFavorite
                )
            }
            entry<StoreNavKey.CoffeeDetail> { key ->
                val coffee = uiState.coffeeById(key.coffeeId)
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