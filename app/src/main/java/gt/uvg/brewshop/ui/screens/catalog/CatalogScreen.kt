package gt.uvg.brewshop.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CatalogScreen(
    coffees: List<Coffee>,
    favoriteIds: Set<String>,
    onOpenCoffee: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    StoreScaffold(
        title = "Catálogo",
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            coffees.forEach { coffee ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = coffee.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = coffee.description,
                            modifier = Modifier.padding(top = 6.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Q${"%.2f".format(coffee.price)}",
                            modifier = Modifier.padding(top = 10.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (coffee.id in favoriteIds) "Favorito" else "No es favorito",
                            modifier = Modifier.padding(top = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Button(
                            onClick = { onToggleFavorite(coffee.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Text(if (coffee.id in favoriteIds) "Quitar favorito" else "Marcar favorito")
                        }
                        Button(
                            onClick = { onOpenCoffee(coffee.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver detalle")
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
    val coffees = listOf(
        Coffee(
            id = "preview-1",
            name = "Bourbon Antigua",
            description = "Cuerpo redondo y dulzor de panela.",
            price = 72.0,
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
            coffees = coffees,
            favoriteIds = setOf("preview-1"),
            onOpenCoffee = {},
            onToggleFavorite = {}
        )
    }
}