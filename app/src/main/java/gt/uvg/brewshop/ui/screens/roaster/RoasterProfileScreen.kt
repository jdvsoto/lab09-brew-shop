package gt.uvg.brewshop.ui.screens.roaster

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.model.Roaster
import gt.uvg.brewshop.ui.components.StoreScaffold
import gt.uvg.brewshop.ui.theme.BrewShopTheme

@Composable
fun RoasterProfileScreen(
    roaster: Roaster,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    StoreScaffold(
        title = roaster.name,
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(roaster.name, style = MaterialTheme.typography.headlineSmall)
                    Text(roaster.role, style = MaterialTheme.typography.titleMedium)
                    Text(roaster.location, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        roaster.description,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoasterProfileScreenPreview() {
    val roaster = Roaster(
        id = "preview-roaster",
        name = "Tostaduria La Bendicion",
        role = "Tostador artesanal",
        location = "Antigua Guatemala",
        description = "Tuesta lotes pequenos con cuidado artesanal."
    )
    BrewShopTheme {
        RoasterProfileScreen(roaster = roaster, onBack = {})
    }
}