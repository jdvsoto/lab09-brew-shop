package gt.uvg.brewshop.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(
    entities = [FavoriteEntity::class, OrderLineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StoreDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    abstract fun orderLineDao(): OrderLineDao

    companion object {
        private const val DATABASE_NAME = "brew_shop.db"

        @Volatile
        private var instance: StoreDatabase? = null

        /**
         * Una sola instancia para toda la app. Se usa applicationContext para no retener una
         * Activity, y AndroidSQLiteDriver porque usa el SQLite del sistema: el Database
         * Inspector no funciona con un SQLite empaquetado en la app.
         */
        fun getInstance(context: Context): StoreDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<StoreDatabase>(
                    context.applicationContext,
                    DATABASE_NAME
                )
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { instance = it }
            }
    }
}
