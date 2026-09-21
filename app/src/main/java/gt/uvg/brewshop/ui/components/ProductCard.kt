package gt.uvg.brewshop.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gt.uvg.brewshop.domain.formatPriceCents
import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.ui.theme.BrewShopTheme

/**
 * Sonda de composicion del paso 2. Se deja en true mientras se toman las mediciones con
 * Logcat y se pone en false antes de la entrega final, que es lo que pide el laboratorio.
 */
private const val COMPOSITION_PROBE_ENABLED = true

private const val PROBE_TAG = "CatalogProbe"

/**
 * Tarjeta de producto del catalogo.
 *
 * Es la misma tarjeta en la version convencional y en la version lazy: solo cambia el
 * contenedor que la coloca. Reserva el espacio de la imagen con [aspectRatio] y fija dos
 * lineas para el nombre, de modo que ambas versiones midan lo mismo y los textos no salten
 * cuando llega la foto.
 */
@Composable
fun ProductCard(
    product: Coffee,
    isFavorite: Boolean,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (COMPOSITION_PROBE_ENABLED) {
        // Registra la entrada a composicion y su salida. La clave es el id: recomponer la
        // tarjeta con el mismo producto no reinicia el efecto, asi que estos logs cuentan
        // entradas y salidas de composicion y no recomposiciones.
        DisposableEffect(product.id) {
            Log.d(PROBE_TAG, "ENTER id=${product.id}")
            onDispose {
                Log.d(PROBE_TAG, "EXIT id=${product.id}")
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product.id) }
    ) {
        // Imagen local durante la comparacion de contenedores: las dos versiones deben
        // medir el mismo trabajo de composicion, sin que la red intervenga. Coil entra
        // despues, ya sobre la version lazy.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatPriceCents(product.priceCents),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (product.stock == 0) "Agotado" else "${product.stock} disponibles",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (product.stock == 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                // IconButton ya ofrece el area tactil minima de 48 x 48 dp.
                IconButton(onClick = { onToggleFavorite(product.id) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) {
                            "Quitar ${product.name} de favoritos"
                        } else {
                            "Marcar ${product.name} como favorito"
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductCardPreview() {
    val product = Coffee(
        id = "preview-1",
        name = "Bourbon Antigua 250 g",
        description = "Cuerpo redondo y dulzor de panela.",
        priceCents = 7200,
        stock = 4,
        imageUrl = "https://picsum.photos/seed/preview-1/400/400",
        roasterId = "preview-roaster",
        origin = "Antigua Guatemala",
        altitude = "1,550 msnm",
        process = "Lavado",
        roastLevel = "Tueste medio",
        tastingNotes = "Panela y cacao"
    )
    BrewShopTheme {
        ProductCard(
            product = product,
            isFavorite = true,
            onProductClick = {},
            onToggleFavorite = {}
        )
    }
}
