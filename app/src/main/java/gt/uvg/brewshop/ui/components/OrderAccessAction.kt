package gt.uvg.brewshop.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Acceso al pedido con el contador de unidades.
 *
 * Vive en la barra superior del catalogo y del detalle, siempre en el mismo lugar, para que
 * el contador no cambie de posicion al navegar. El numero sale de las lineas del pedido, no
 * de un contador propio de la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAccessAction(
    unitCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // IconButton ya reserva el area tactil minima de 48 x 48 dp.
    IconButton(onClick = onClick, modifier = modifier) {
        BadgedBox(
            badge = {
                if (unitCount > 0) {
                    Badge { Text(unitCount.toString()) }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = if (unitCount == 0) {
                    "Ver pedido, sin unidades"
                } else {
                    "Ver pedido, $unitCount unidades"
                }
            )
        }
    }
}
