package gt.uvg.brewshop.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme

@Composable
fun CoffeeDetailScreen(
    coffee: Coffee,
    roasterName: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenRoaster: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    StoreScaffold(
        title = coffee.name,
        modifier = modifier,
        onBack = onBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(coffee.description, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Q${"%.2f".format(coffee.price)}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isFavorite) "Favorito" else "No es favorito",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Button(onClick = onToggleFavorite, modifier = Modifier.fillMaxWidth()) {
                Text(if (isFavorite) "Quitar favorito" else "Marcar favorito")
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tostador", style = MaterialTheme.typography.labelLarge)
                    Text(roasterName, style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onOpenRoaster) {
                        Text("Ver perfil del tostador")
                    }
                }
            }
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Ocultar ficha técnica" else "Ficha técnica")
            }
            if (expanded) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ficha técnica", style = MaterialTheme.typography.titleMedium)
                        Text("Origen: ${coffee.origin}")
                        Text("Altitud: ${coffee.altitude}")
                        Text("Proceso: ${coffee.process}")
                        Text("Nivel de tueste: ${coffee.roastLevel}")
                        Text("Notas de cata: ${coffee.tastingNotes}")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoffeeDetailScreenPreview() {
    val coffee = Coffee(
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
    BrewShopTheme {
        CoffeeDetailScreen(
            coffee = coffee,
            roasterName = "Tostaduria La Bendicion",
            isFavorite = true,
            onToggleFavorite = {},
            onOpenRoaster = {},
            onBack = {}
        )
    }
}