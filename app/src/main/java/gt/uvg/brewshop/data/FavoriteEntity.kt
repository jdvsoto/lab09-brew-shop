package gt.uvg.brewshop.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Un producto marcado como favorito.
 *
 * La clave es el id del producto: el catalogo ya tiene IDs estables, asi que no hace falta
 * autogenerar una, y un mismo producto no puede quedar marcado dos veces.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: String
)
