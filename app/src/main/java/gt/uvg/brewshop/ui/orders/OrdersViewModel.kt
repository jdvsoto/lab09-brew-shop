package gt.uvg.brewshop.ui.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gt.uvg.brewshop.network.StoreApi
import gt.uvg.brewshop.network.toPlacedOrder
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

private const val TAG = "OrdersViewModel"
private const val ORDERS_SORT_FIELD = "createdAt"
private const val ORDERS_SORT_DIRECTION = "desc"

/**
 * ViewModel de la feature "Mis pedidos", separada de la tienda.
 *
 * Las cargas no se lanzan en init ni desde la UI: responden al evento que abre cada pantalla
 * o a un reintento. Por eso rotar no repite la solicitud, y un pedido recien enviado aparece
 * al abrir la lista, que siempre se pide de nuevo.
 */
class OrdersViewModel : ViewModel() {

    private val orderApi = StoreApi.orderApi

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
            _ordersUiState.value = try {
                // El orden lo resuelve el servidor, no la app.
                val orders = orderApi
                    .getOrders(sortBy = ORDERS_SORT_FIELD, order = ORDERS_SORT_DIRECTION)
                    .map { it.toPlacedOrder() }
                if (orders.isEmpty()) OrdersUiState.Empty else OrdersUiState.Success(orders)
            } catch (e: IOException) {
                Log.w(TAG, "Sin respuesta al cargar los pedidos", e)
                OrdersUiState.Error("No pudimos cargar tus pedidos. Revisa tu conexión.")
            } catch (e: HttpException) {
                Log.w(TAG, "El servidor respondio ${e.code()} al cargar los pedidos", e)
                // MockAPI responde 404 en vez de una lista vacia cuando un filtro no coincide.
                if (e.code() == 404) {
                    OrdersUiState.Empty
                } else {
                    OrdersUiState.Error("El servidor no pudo mostrar tus pedidos.")
                }
            } catch (e: SerializationException) {
                Log.w(TAG, "Respuesta inesperada al cargar los pedidos", e)
                OrdersUiState.Error("Recibimos una respuesta inesperada del servidor.")
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        // Una carga anterior puede ser de otro pedido: se cancela para no mostrar el equivocado.
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _orderDetailUiState.value = OrderDetailUiState.Loading
            _orderDetailUiState.value = try {
                OrderDetailUiState.Success(orderApi.getOrder(orderId).toPlacedOrder())
            } catch (e: IOException) {
                Log.w(TAG, "Sin respuesta al cargar el pedido $orderId", e)
                OrderDetailUiState.Error("No pudimos cargar el pedido. Revisa tu conexión.")
            } catch (e: HttpException) {
                Log.w(TAG, "El servidor respondio ${e.code()} al cargar el pedido $orderId", e)
                if (e.code() == 404) {
                    OrderDetailUiState.NotFound
                } else {
                    OrderDetailUiState.Error("El servidor no pudo mostrar el pedido.")
                }
            } catch (e: SerializationException) {
                Log.w(TAG, "Respuesta inesperada al cargar el pedido $orderId", e)
                OrderDetailUiState.Error("Recibimos una respuesta inesperada del servidor.")
            }
        }
    }
}
