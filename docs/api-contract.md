# Contrato de la API de pedidos

API propia de la tienda, creada en MockAPI. La app la consume con Retrofit desde
`network/StoreApi.kt`, que es el unico lugar donde vive la URL base.

## URL base

```
https://6acee031f338359cf1c3b89b.mockapi.io/api/v1/
```

No requiere autenticacion. Cualquiera que conozca la URL puede leer, modificar y borrar
los pedidos, por eso la app no envia datos personales.

## Rutas que usa la app

| Metodo | Ruta | Uso |
|---|---|---|
| `POST` | `/orders` | Enviar un pedido. Responde 201 con el pedido y su `id` |
| `GET` | `/orders?sortBy=createdAt&order=desc` | "Mis pedidos", del mas reciente al mas antiguo |
| `GET` | `/orders/{id}` | Detalle de un pedido. Responde 404 si ya no existe |

El orden de "Mis pedidos" lo hace el servidor con `sortBy` y `order`; la app no reordena.

## Campos del recurso `orders`

| Campo | Tipo en MockAPI | Quien lo asigna |
|---|---|---|
| `id` | texto (automatico) | Servidor |
| `createdAt` | Number | App |
| `lines` | Array | App |
| `unitCount` | Number | App |
| `totalCents` | Number | App |
| `billingType` | String | App |
| `paymentMethod` | String | App |

Cada elemento de `lines` tiene: `productId` (texto), `name` (texto), `unitPriceCents`,
`quantity` y `subtotalCents` (enteros).

## Ejemplo real

Respuesta de `GET /orders/2`, el pedido creado con `docs/api-sample-request.json`. Es el
cuerpo tal como lo devolvio MockAPI (aqui indentado para leerlo; el contenido es el mismo):

```json
{
  "createdAt": 1791907200000,
  "lines": [
    {
      "productId": "c2",
      "name": "Geisha Huehuetenango",
      "unitPriceCents": 14500,
      "quantity": 2,
      "subtotalCents": 29000
    },
    {
      "productId": "c1",
      "name": "Bourbon Antigua",
      "unitPriceCents": 7200,
      "quantity": 1,
      "subtotalCents": 7200
    }
  ],
  "unitCount": 3,
  "totalCents": 36200,
  "billingType": "CF",
  "paymentMethod": "CASH_ON_DELIVERY",
  "id": "2"
}
```

## Decisiones

| Decision | Que se eligio | Por que |
|---|---|---|
| Que guarda cada linea | `productId`, `name`, `unitPriceCents`, `quantity` y `subtotalCents` | El catalogo se genera en la app. Si la linea solo guardara el id, "Mis pedidos" mostraria el nombre y el precio actuales, no los de la compra. Con una copia de nombre y precio, el pedido se reconstruye sin depender del catalogo |
| Datos personales | No se envian nombre, telefono, NIT ni razon social. Solo el tipo de facturacion (`"CF"` o `"NIT"`) y el metodo de pago | La API es publica para quien conozca la URL. "Mis pedidos" no necesita datos personales, asi que no salen del telefono |
| Dinero | Enteros en centavos (`totalCents`, `unitPriceCents`, `subtotalCents`) | Es la representacion del Lab 10: la aritmetica es exacta y se formatea con `formatPriceCents` solo al mostrar |
| Fecha | `createdAt`, Number con milisegundos desde 1970 (UTC), asignado por la app al enviar | Si falta, MockAPI completa la fecha con un valor al azar. Como numero, `sortBy=createdAt` ordena de forma confiable; un `id` de texto no ("9" queda antes que "11") |
| Numero de pedido | El `id` que asigna el servidor sustituye al folio local del Lab 11 en la confirmacion | El folio local es un contador en memoria que vuelve a `#ORD-00001` al reiniciar la app; el id del servidor es unico y persistente |

## Lo observado al probar la API

- `POST /orders` respondio **201** con el pedido guardado completo, incluidas sus lineas, y
  un `id` de texto asignado por el servidor (`"2"`).
- Los valores numericos vuelven como numeros (por ejemplo `36200`), no como texto, asi
  que los DTO con `Int` y `Long` coinciden con lo que responde la API.
- El unico campo que agrega el servidor es `id`.
- `GET /orders?sortBy=createdAt&order=desc` respondio **200** y devolvio el pedido de
  `createdAt` mayor primero.
- `GET /orders/999999` respondio **404** con el cuerpo `"Not found"`. En Retrofit llega
  como `HttpException` con `code() == 404`.
- El recurso se creo primero con un campo `Lines` (con L mayuscula) en lugar de `lines`:
  el servidor guardaba lo que enviaba la app en un campo `lines` aparte y dejaba `Lines`
  vacio. Se renombro a `lines` y se verifico de nuevo.
- Al editar el esquema, MockAPI regenero el registro `id` 1 con datos de relleno
  (`createdAt` 26, `billingType` "billingType 1"). No es un pedido real; el modelo lo
  tolera porque los valores de facturacion y pago desconocidos quedan en `null`.
