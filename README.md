# Carrito de Compras — Arquitectura de Microservicios

Carrito de compras y facturación, implementado con una arquitectura de microservicios (Spring Boot /
Spring Cloud).

## Arquitectura

| Servicio | Puerto | Descripción |
|---|---|---|
| `registry-service` | 8761 | Registro y descubrimiento (Eureka) |
| `config-server` | 8888 | Configuración centralizada (lee de [config-data](https://github.com/duckpie3/config-data)) |
| `gateway-service` | 8080 | Punto único de entrada; enruta `/product`, `/cart-item` e `/invoice` |
| `admin-service` | 9090 | Administración y monitoreo |
| `auth-service` | 8081 | Autenticación |
| `invoice-service` | 8084 | API de productos, carrito y facturación |

## Requisitos

- Java 21 (no se necesita Maven instalado; cada servicio incluye el wrapper `mvnw`).
- La API Invoice usa una base de datos H2 en memoria: **los datos se reinician al
  reiniciar el servicio**.

## Opción rápida: probar solo la API Invoice

Para probar la funcionalidad del carrito y la facturación basta con levantar
`invoice-service`:

```bash
cd services/invoice-service
sh mvnw spring-boot:run
```

El servicio queda en `http://localhost:8084`.

### Tokens de prueba

Todas las peticiones requieren un token JWT (el cliente se identifica con el ID
contenido en el token). Tokens de prueba ya firmados con el secreto de desarrollo:

```bash
# Administrador (id 1): puede crear/editar/eliminar productos
ADMIN='eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlkIjoxLCJyb2xlcyI6W3siYXV0aG9yaXR5IjoiQURNSU4ifV0sImV4cCI6MjA1MDAwMDAwMH0.ytDP_B-V4qguThfG7kZB_JM-NepZTtiah9ncEtsMYFw'

# Cliente (id 10): carrito y compra
CUST='eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lciIsImlkIjoxMCwicm9sZXMiOlt7ImF1dGhvcml0eSI6IkNVU1RPTUVSIn1dLCJleHAiOjIwNTAwMDAwMDB9.vZ61HynhP2jcd9_ZFfI2v3oRWDkcV728K8YMLsmh7oY'
```

### Flujo completo con curl

```bash
# 1. Crear un producto (solo ADMIN)
curl -X POST http://localhost:8084/product \
  -H "Authorization: Bearer $ADMIN" \
  -H 'Content-Type: application/json' \
  -d '{"gtin":"7501055300075","name":"Coca-cola 600 ml","price":21.00,"stock":100}'

# 2. Consultar productos
curl http://localhost:8084/product -H "Authorization: Bearer $CUST"

# 3. Agregar 2 Coca-Colas al carrito
curl -X POST http://localhost:8084/cart-item \
  -H "Authorization: Bearer $CUST" \
  -H 'Content-Type: application/json' \
  -d '{"gtin":"7501055300075","quantity":2}'

# 4. Consultar el carrito (muestra nombre, costo unitario y cantidad)
curl http://localhost:8084/cart-item -H "Authorization: Bearer $CUST"

# 5. Finalizar la compra: genera la factura, descuenta stock (100 → 98) y vacía el carrito
curl -X POST http://localhost:8084/invoice -H "Authorization: Bearer $CUST"

# 6. Consultar facturas
curl http://localhost:8084/invoice -H "Authorization: Bearer $CUST"

# 7. Detalle de la factura: total 42.00, impuestos 6.72 (16%), subtotal 35.28
curl http://localhost:8084/invoice/1 -H "Authorization: Bearer $CUST"

# 8. Eliminar un artículo del carrito (usar el "id" que regresa el paso 4)
curl -X DELETE http://localhost:8084/cart-item/{id} -H "Authorization: Bearer $CUST"

# 9. Vaciar el carrito
curl -X DELETE http://localhost:8084/cart-item -H "Authorization: Bearer $CUST"
```

Casos de error que se pueden probar:

```bash
# Sin token → 401
curl -i http://localhost:8084/cart-item

# Cliente intenta crear un producto → 403
curl -i -X POST http://localhost:8084/product -H "Authorization: Bearer $CUST" \
  -H 'Content-Type: application/json' -d '{"gtin":"1","name":"x","price":1,"stock":1}'

# Cantidad mayor al stock → 400 "Stock insuficiente"
curl -i -X POST http://localhost:8084/cart-item -H "Authorization: Bearer $CUST" \
  -H 'Content-Type: application/json' -d '{"gtin":"7501055300075","quantity":1000}'
```

### Interfaces web

- **Swagger UI**: <http://localhost:8084/swagger-ui.html> — botón *Authorize* y pegar
  el token (sin el prefijo `Bearer`).
- **Consola H2**: <http://localhost:8084/h2-console> — JDBC URL `jdbc:h2:mem:db_invoice`,
  usuario `sa`, contraseña vacía.

### Pruebas automatizadas

```bash
cd services/invoice-service
sh mvnw test
```

Las pruebas de integración cubren el CRUD de productos, el ciclo del carrito,
la validación de stock y la generación de la factura con el ejemplo de la
especificación (2 × $21 → total 42.00, impuestos 6.72, subtotal 35.28).

## Arquitectura completa (todos los microservicios)

Levantar cada servicio en una terminal distinta, **en este orden**
(`sh mvnw spring-boot:run` dentro de cada carpeta en `services/`):

1. `config-server` (8888) — los demás obtienen su configuración de aquí
2. `registry-service` (8761) — Eureka
3. `invoice-service` (8084)
4. `gateway-service` (8080)
5. `admin-service` (9090) y `auth-service` (8081) — opcionales para el flujo de compra

Verificar en el dashboard de Eureka (<http://localhost:8761>) que los servicios
estén registrados. Después, las mismas peticiones `curl` funcionan a través del
gateway cambiando el puerto a **8080**, por ejemplo:

```bash
curl http://localhost:8080/product -H "Authorization: Bearer $CUST"
```
