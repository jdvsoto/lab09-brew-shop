package gt.uvg.brewshop.domain

import gt.uvg.brewshop.model.Roaster

/**
 * Tostadores de la tienda. Son datos fijos de la app, no se leen de ninguna fuente: los
 * productos de Firestore los referencian por [Roaster.id].
 */
val storeRoasters = listOf(
    Roaster(
        id = "r1",
        name = "Tostaduria La Bendicion",
        role = "Tostador artesanal",
        location = "Antigua Guatemala, Sacatepequez",
        description = "Taller familiar de tercera generacion. Tuesta en lotes de ocho " +
            "kilos y deja descansar cada lote cinco dias antes de venderlo, para que los " +
            "azucares del grano terminen de asentarse."
    ),
    Roaster(
        id = "r2",
        name = "Xelaju Roasters",
        role = "Micro-tostador de altura",
        location = "Quetzaltenango",
        description = "Compra directamente a doce productores del altiplano y paga sobre " +
            "el precio de bolsa. Trabaja tuestes claros para que se note el origen de " +
            "cada finca en la taza."
    ),
    Roaster(
        id = "r3",
        name = "Cardamomo Cafe",
        role = "Tostador de origen unico",
        location = "Coban, Alta Verapaz",
        description = "Nacio dentro de una finca de cardamomo y hoy tuesta solo lotes de " +
            "las Verapaces. Publica la fecha de tueste de cada bolsa."
    ),
    Roaster(
        id = "r4",
        name = "Volcan Tostadores",
        role = "Tostador experimental",
        location = "Ciudad de Guatemala",
        description = "Trabaja procesos anaerobicos y fermentaciones controladas. Cada " +
            "lote lleva la ficha de su fermentacion en la etiqueta."
    ),
    Roaster(
        id = "r5",
        name = "Finca Los Cipreses",
        role = "Productor que tuesta su cosecha",
        location = "Acatenango, Chimaltenango",
        description = "Cultiva, despulpa y tuesta en la misma finca. Vende unicamente lo " +
            "que produce, asi que su catalogo cambia con cada cosecha."
    ),
    Roaster(
        id = "r6",
        name = "Ruta del Altiplano",
        role = "Tostador cooperativo",
        location = "San Marcos",
        description = "Reune a cuarenta pequenos caficultores de la boca costa. El " +
            "excedente de cada venta regresa a la cooperativa."
    )
)
