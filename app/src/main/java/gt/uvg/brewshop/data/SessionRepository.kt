package gt.uvg.brewshop.data

/** Sesion del usuario. Las reglas de Firestore leen su uid en request.auth. */
interface SessionRepository {

    /** Garantiza una sesion anonima. Si ya existe una, no crea otra. */
    suspend fun signInAnonymously(): Result<Unit>
}
