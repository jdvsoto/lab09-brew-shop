package gt.uvg.brewshop.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * [floatingActionButton] es opcional porque solo el catalogo necesita el FAB "Volver
 * arriba"; el detalle y el perfil del tostador siguen sin uno. Por defecto no dibuja nada,
 * igual que el `Scaffold` de Material3 que envuelve.
 *
 * [actions] da a la barra superior una posicion estable para el acceso al pedido, de modo
 * que el contador de unidades ocupe el mismo lugar en el catalogo y en el detalle.
 * [snackbarHost] permite mostrar la confirmacion o el rechazo del pedido sin salir de la
 * pantalla en la que se produjo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar"
                            )
                        }
                    }
                },
                actions = actions
            )
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        content = content
    )
}
