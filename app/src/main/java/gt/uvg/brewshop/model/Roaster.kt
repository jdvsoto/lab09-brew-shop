package gt.uvg.brewshop.model

/**
 * El tostador asociado a uno o varios cafes. Es el perfil que muestra la tercera pantalla.
 */
data class Roaster(
    val id: String,
    val name: String,
    val role: String,
    val location: String,
    val description: String
)
