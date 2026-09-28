package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.Coffee
import gt.uvg.brewshop.model.Roaster
import kotlin.random.Random

/** Tamano total del catalogo de prueba, incluidos los productos originales. */
const val CATALOG_SIZE = 500

/**
 * Semilla fija. Con ella la lista generada es identica en cada ejecucion: buscar,
 * navegar, hacer scroll o rotar no cambia los productos, sus precios ni sus IDs.
 */
private const val CATALOG_SEED = 20260920L

/**
 * Prefijo de los IDs generados. Los productos originales usan "c1".."c4", de modo que
 * este prefijo no colisiona con ellos y permite distinguirlos de un vistazo.
 */
private const val GENERATED_ID_PREFIX = "gen-"

private val origins = listOf(
    "Antigua", "Huehuetenango", "Atitlan", "Coban",
    "Fraijanes", "Nuevo Oriente", "San Marcos", "Acatenango"
)

private val varieties = listOf(
    "Bourbon", "Caturra", "Typica", "Catuai",
    "Pacamara", "Geisha", "Maragogipe", "Villa Sarchi"
)

private val processes = listOf("Lavado", "Honey", "Natural", "Anaerobico")

private val roastLevels = listOf("Tueste claro", "Tueste medio", "Tueste oscuro")

private val tastingNotes = listOf(
    "panela", "cacao", "naranja madura", "jazmin", "durazno", "bergamota",
    "almendra", "miel", "manzana roja", "fresa", "vainilla", "caramelo",
    "nuez moscada", "mandarina", "chocolate amargo", "ciruela"
)

private val brewMethods = listOf(
    "espresso", "prensa francesa", "V60", "chemex", "aeropress", "moka"
)

/** Presentaciones de venta. El precio crece con el tamano, con descuento por volumen. */
private enum class Presentation(
    val label: String,
    val priceNumerator: Int,
    val priceDenominator: Int
) {
    QUARTER_KILO("250 g", 10, 10),
    HALF_KILO("500 g", 19, 10),
    ONE_KILO("1 kg", 36, 10)
}

/**
 * Construye el catalogo completo conservando [originalProducts] tal como estan y
 * generando solo los que faltan hasta llegar a [size].
 *
 * Los IDs generados son deterministas y estables, y cada producto queda asociado a un
 * tostador que ya existe en [roasters].
 */
fun buildCatalog(
    originalProducts: List<Coffee>,
    roasters: List<Roaster>,
    size: Int = CATALOG_SIZE
): List<Coffee> {
    require(roasters.isNotEmpty()) { "El catalogo necesita al menos un tostador." }
    if (originalProducts.size >= size) return originalProducts

    val random = Random(CATALOG_SEED)
    val generated = (originalProducts.size until size).map { index ->
        generateProduct(index, random, roasters)
    }
    return originalProducts + generated
}

/**
 * URL estable de la imagen de un producto. Depende solo del ID, de modo que hacer scroll
 * o recomponer la tarjeta pide siempre la misma imagen y la cache de Coil puede servirla.
 */
fun imageUrlFor(productId: String): String =
    "https://picsum.photos/seed/$productId/400/400"

private fun generateProduct(index: Int, random: Random, roasters: List<Roaster>): Coffee {
    val id = GENERATED_ID_PREFIX + index.toString().padStart(4, '0')
    val origin = origins.random(random)
    val variety = varieties.random(random)
    val process = processes.random(random)
    val roastLevel = roastLevels.random(random)
    val presentation = Presentation.entries.random(random)
    val brewMethod = brewMethods.random(random)
    val notes = pickNotes(random)

    return Coffee(
        id = id,
        name = "$variety $origin ${presentation.label}",
        description = "Cafe de $origin, proceso ${process.lowercase()} en presentacion de " +
            "${presentation.label}. ${roastLevel} recomendado para $brewMethod.",
        priceCents = priceCentsFor(variety, presentation, random),
        stock = stockFor(index, random),
        imageUrl = imageUrlFor(id),
        roasterId = roasters.random(random).id,
        origin = origin,
        altitude = formatAltitude(random.nextInt(12, 21) * 100),
        process = process,
        roastLevel = roastLevel,
        tastingNotes = notes
    )
}

/**
 * Precio base por variedad, ajustado por presentacion. Las variedades escasas cuestan
 * mas, de modo que los precios del catalogo generado siguen teniendo sentido.
 */
private fun priceCentsFor(
    variety: String,
    presentation: Presentation,
    random: Random
): Int {
    val basePriceCents = when (variety) {
        "Geisha" -> random.nextInt(13000, 18000)
        "Pacamara", "Maragogipe" -> random.nextInt(9000, 12000)
        else -> random.nextInt(5500, 8500)
    }
    return basePriceCents * presentation.priceNumerator / presentation.priceDenominator
}

/**
 * Reparte las existencias de forma determinista y garantiza que el catalogo incluya
 * productos agotados y productos con exactamente 3 unidades, que son los casos que pide
 * comprobar el limite del pedido.
 */
private fun stockFor(index: Int, random: Random): Int = when (index % 9) {
    0 -> 0
    1 -> 3
    else -> random.nextInt(1, 41)
}

private fun pickNotes(random: Random): String {
    val selected = LinkedHashSet<String>()
    while (selected.size < 3) {
        selected.add(tastingNotes.random(random))
    }
    return selected.joinToString(", ").replaceFirstChar { it.uppercase() }
}

private fun formatAltitude(meters: Int): String =
    "${meters / 1000},${(meters % 1000).toString().padStart(3, '0')} msnm"
