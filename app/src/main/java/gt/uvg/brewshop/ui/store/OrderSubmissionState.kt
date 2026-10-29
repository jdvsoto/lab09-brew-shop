package gt.uvg.brewshop.ui.store

/** Estado del envio del pedido al servidor. */
sealed interface OrderSubmissionState {

    /** No hay un envio en curso. */
    data object Idle : OrderSubmissionState

    /** Esperando la respuesta del servidor. */
    data object Sending : OrderSubmissionState

    /** El servidor guardo el pedido y le asigno [orderId]. */
    data class Sent(val orderId: String) : OrderSubmissionState

    /** El envio fallo. [message] es un texto para la persona, nunca un detalle tecnico. */
    data class Failed(val message: String) : OrderSubmissionState
}
