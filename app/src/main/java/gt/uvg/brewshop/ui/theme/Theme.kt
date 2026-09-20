package gt.uvg.brewshop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CoffeeLatte,
    onPrimary = CoffeeBark,
    secondary = CoffeeCopper,
    tertiary = CoffeeGold,
    background = CoffeeBark,
    surface = Color(0xFF2E1D17),
    onSurface = CoffeeCream
)

private val LightColorScheme = lightColorScheme(
    primary = CoffeeRoast,
    secondary = CoffeeGold,
    tertiary = CoffeeCopper,
    background = CoffeeCream,
    surface = Color(0xFFFFFBF8)
)

@Composable
fun BrewShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
