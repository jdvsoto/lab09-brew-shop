package gt.uvg.brewshop.model

/** Por que fallo una lectura o una escritura, sin importar de que fuente venian los datos. */
enum class DataErrorReason {
    NO_CONNECTION,
    NOT_FOUND,
    OUT_OF_STOCK,
    NOT_SIGNED_IN,
    SERVER,
    UNEXPECTED_RESPONSE,
    STORAGE
}

/**
 * Fallo que los Repository devuelven dentro de un Result. Los ViewModels solo leen [reason]:
 * no conocen las excepciones de Firestore, Retrofit, Room ni DataStore que lo produjeron.
 */
class DataException(
    val reason: DataErrorReason,
    cause: Throwable? = null
) : Exception(reason.name, cause)

/** Motivo de cualquier fallo; uno que no venga de un Repository se trata como inesperado. */
val Throwable.dataErrorReason: DataErrorReason
    get() = (this as? DataException)?.reason ?: DataErrorReason.UNEXPECTED_RESPONSE
