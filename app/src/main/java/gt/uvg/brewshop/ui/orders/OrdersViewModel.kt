package gt.uvg.brewshop.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gt.uvg.brewshop.BrewShopApplication
import gt.uvg.brewshop.data.PlacedOrderRepository
import gt.uvg.brewshop.model.DataErrorReason
import gt.uvg.brewshop.model.dataErrorReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de la feature "Mis pedidos", separada de la tienda. Recibe su Repository por
 * constructor y no sabe que los pedidos viven en MockAPI.
 *
 * Las cargas no se lanzan en init ni desde la UI: responden al evento que abre cada pantalla
 * o a un reintento. Por eso rotar no repite la solicitud, y un pedido recien enviado aparece
 * al abrir la lista, que siempre se pide de nuevo.
 */
class OrdersViewModel(private val placedOrderRepository: PlacedOrderRepository) : ViewModel() {

    private val _ordersUiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val ordersUiState: StateFlow<OrdersUiState> = _ordersUiState.asStateFlow()

    private val _orderDetailUiState = MutableStateFlow<OrderDetailUiState>(OrderDetailUiState.Loading)
    val orderDetailUiState: StateFlow<OrderDetailUiState> = _orderDetailUiState.asStateFlow()

    private var ordersJob: Job? = null
    private var detailJob: Job? = null

    fun loadOrders() {
        // Si ya hay una carga en curso no se lanza otra: un toque repetido no duplica la solicitud.
        if (ordersJob?.isActive == true) return
        ordersJob = viewModelScope.launch {
            _ordersUiState.value = OrdersUiState.Loading
            _ordersUiState.value = placedOrderRepository.getOrders().fold(
                onSuccess = { orders ->
                    if (orders.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(orders)
                },
                onFailure = { error ->
                    OrdersUiState.Error(
                        when (error.dataErrorReason) {
                            DataErrorReason.NO_CONNECTION -> "No pudimos cargar tus pedidos. Revisa tu conexión."
                            DataErrorReason.UNEXPECTED_RESPONSE -> "Recibimos una respuesta inesperada del servidor."
                            else -> "El servidor no pudo mostrar tus pedidos."
                        }
                    )
                }
            )
        }
    }

    fun loadOrderDetail(orderId: String) {
        // Una carga anterior puede ser de otro pedido: se cancela para no mostrar el equivocado.
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _orderDetailUiState.value = OrderDetailUiState.Loading
            _orderDetailUiState.value = placedOrderRepository.getOrder(orderId).fold(
                onSuccess = { order -> OrderDetailUiState.Success(order) },
                onFailure = { error ->
                    when (error.dataErrorReason) {
                        DataErrorReason.NOT_FOUND -> OrderDetailUiState.NotFound
                        DataErrorReason.NO_CONNECTION ->
                            OrderDetailUiState.Error("No pudimos cargar el pedido. Revisa tu conexión.")
                        DataErrorReason.UNEXPECTED_RESPONSE ->
                            OrderDetailUiState.Error("Recibimos una respuesta inesperada del servidor.")
                        else -> OrderDetailUiState.Error("El servidor no pudo mostrar el pedido.")
                    }
                }
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BrewShopApplication
                OrdersViewModel(application.container.placedOrderRepository)
            }
        }
    }
}
