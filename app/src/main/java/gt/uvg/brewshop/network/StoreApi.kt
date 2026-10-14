package gt.uvg.brewshop.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

object StoreApi {

    // El unico lugar donde vive la URL base. Termina en "/" porque las rutas son relativas.
    private const val BASE_URL = "https://6acee031f338359cf1c3b89b.mockapi.io/api/v1/"

    // MockAPI puede devolver campos que el DTO no declara: sin ignoreUnknownKeys, la
    // deserializacion fallaria con SerializationException.
    internal val json = Json { ignoreUnknownKeys = true }

    val orderApi: OrderApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }
}
