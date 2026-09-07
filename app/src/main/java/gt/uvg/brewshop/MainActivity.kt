package gt.uvg.brewshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import gt.uvg.brewshop.ui.navigation.StoreNavigation
import gt.uvg.brewshop.ui.theme.BrewShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrewShopTheme {
                StoreNavigation()
            }
        }
    }
}
