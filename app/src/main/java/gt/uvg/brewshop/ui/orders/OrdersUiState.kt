package gt.uvg.brewshop.ui.orders

import gt.uvg.brewshop.model.PlacedOrder

/** Estado de "Mis pedidos". Al ser sealed, la pantalla tiene que resolver cada caso. */
sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Error(val message: String) : OrdersUiState
    data class Success(val orders: List<PlacedOrder>) : OrdersUiState
}

/** Estado del detalle de un pedido, solicitado al servidor por su id. */
sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState
    data object NotFound : OrderDetailUiState
    data class Error(val message: String) : OrderDetailUiState
    data class Success(val order: PlacedOrder) : OrderDetailUiState
}
