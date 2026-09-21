package gt.uvg.brewshop.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.components.ProductCard
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme
import kotlinx.coroutines.launch

/** A partir de este umbral de tarjetas desplazadas aparece el FAB "Volver arriba". */
private const val SCROLL_TO_TOP_THRESHOLD = 4

/** Espacio inferior reservado para que el FAB no tape la ultima fila de la cuadricula. */
private val BOTTOM_CONTENT_PADDING = 96.dp

/**
 * Pantalla del catalogo: busqueda, contador de resultados, estado vacio y la cuadricula
 * lazy de dos columnas con clave por id.
 *
 * Es stateless: [query] y [gridState] viven fuera (el ViewModel y el composable que
 * envuelve a NavDisplay, respectivamente), por lo que la posicion de scroll sobrevive a
 * ir al detalle y volver, y la consulta se reinicia solo cuando cambia de verdad, no cada
 * vez que esta pantalla vuelve a componerse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<Coffee>,
    query: String,
    resultCount: Int,
    catalogSize: Int,
    hasNoResults: Boolean,
    favoriteIds: Set<String>,
    gridState: LazyGridState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenCoffee: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    // En la version convencional el scroll lo lleva un ScrollState, no el LazyGridState.
    val scrollState = rememberScrollState()
    val showScrollToTop by remember {
        derivedStateOf { scrollState.value > 0 }
    }

    StoreScaffold(
        title = "Mi tienda",
        modifier = modifier,
        floatingActionButton = {
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch { scrollState.animateScrollTo(0) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Volver arriba"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Buscar productos") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = "$resultCount de $catalogSize productos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (hasNoResults) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No encontramos productos.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onClearQuery) {
                        Text("Limpiar búsqueda")
                    }
                }
            } else {
                // VERSION CONVENCIONAL (paso 2): construye las 250 filas de una vez, con
                // las mismas tarjetas y dimensiones que la version lazy. Solo cambia el
                // contenedor, para que la comparacion de Logcat mida el contenedor y no
                // otra diferencia.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = BOTTOM_CONTENT_PADDING
                        ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    products.chunked(2).forEach { rowProducts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowProducts.forEach { product ->
                                ProductCard(
                                    product = product,
                                    isFavorite = product.id in favoriteIds,
                                    onProductClick = onOpenCoffee,
                                    onToggleFavorite = onToggleFavorite,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowProducts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    val products = listOf(
        Coffee(
            id = "preview-1",
            name = "Bourbon Antigua",
            description = "Cuerpo redondo y dulzor de panela.",
            priceCents = 7200,
            stock = 12,
            imageUrl = "https://picsum.photos/seed/preview-1/400/400",
            roasterId = "preview-roaster",
            origin = "Antigua Guatemala",
            altitude = "1,550 msnm",
            process = "Lavado",
            roastLevel = "Tueste medio",
            tastingNotes = "Panela y cacao"
        )
    )
    BrewShopTheme {
        CatalogScreen(
            products = products,
            query = "",
            resultCount = products.size,
            catalogSize = products.size,
            hasNoResults = false,
            favoriteIds = setOf("preview-1"),
            gridState = rememberLazyGridState(),
            onQueryChange = {},
            onClearQuery = {},
            onOpenCoffee = {},
            onToggleFavorite = {}
        )
    }
}
