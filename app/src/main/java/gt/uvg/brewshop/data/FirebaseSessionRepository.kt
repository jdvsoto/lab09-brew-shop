package gt.uvg.brewshop.data

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.DataException
import kotlinx.coroutines.tasks.await

class FirebaseSessionRepository(private val auth: FirebaseAuth) : SessionRepository {

    override suspend fun signInAnonymously(): Result<Unit> {
        // Firebase conserva la sesion anonima entre aperturas: se reutiliza el mismo uid.
        if (auth.currentUser != null) return Result.success(Unit)
        return try {
            auth.signInAnonymously().await()
            Result.success(Unit)
        } catch (e: FirebaseNetworkException) {
            Result.failure(DataException(DataErrorReason.NO_CONNECTION, e))
        } catch (e: FirebaseException) {
            Result.failure(DataException(DataErrorReason.NOT_SIGNED_IN, e))
        }
    }
}
