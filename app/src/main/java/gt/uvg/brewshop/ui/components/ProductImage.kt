package gt.uvg.brewshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/** Estado visual de la imagen. Pertenece a la UI, no al ViewModel. */
private enum class ImageState { Loading, Success, Error }

/**
 * Imagen remota de un producto con sus tres estados.
 *
 * El [Box] reserva el espacio antes de que exista la imagen, de modo que el texto de la
 * tarjeta no salta cuando la descarga termina. El skeleton es una decision de esta UI:
 * activar crossfade no lo crea, solo suaviza la aparicion de la foto ya descargada.
 *
 * El estado se recuerda con [imageUrl] como clave, asi que reciclar la tarjeta para otro
 * producto reinicia la espera en lugar de mostrar el estado de la imagen anterior.
 */
@Composable
fun ProductImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var imageState by remember(imageUrl) { mutableStateOf(ImageState.Loading) }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // AsyncImage permanece siempre compuesto. Si solo se dibujara en el caso Success
        // nunca iniciaria la carga ni recibiria sus callbacks, y la imagen no llegaria.
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onLoading = { imageState = ImageState.Loading },
            onSuccess = { imageState = ImageState.Success },
            onError = { imageState = ImageState.Error }
        )

        when (imageState) {
            // Bloque neutro sobre el espacio reservado mientras Coil espera la imagen.
            ImageState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // El error afecta solo a esta imagen: el resto de la tarjeta sigue usable.
            ImageState.Error -> Text(
                text = "Imagen no disponible",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )

            // En Success, AsyncImage ya muestra la imagen con su fundido.
            ImageState.Success -> Unit
        }
    }
}
