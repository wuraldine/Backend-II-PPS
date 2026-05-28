# Arquitectura y flujo del sistema

## ¿Qué hace este sistema?

Backend de tienda online (e-commerce de productos informáticos) construido en **Java 17 + Spring Boot** con arquitectura en capas. Permite:

- Navegar el catálogo sin cuenta (sesión de invitado)
- Registrarse, iniciar/cerrar sesión
- Agregar productos al carrito (incluso como invitado)
- Hacer checkout (solo usuarios registrados)
- Gestión de direcciones y órdenes
- Panel de administración para productos y usuarios

---

## Arquitectura en capas

```
HTTP Request
     │
     ▼
[Controller]          web/controller/  → recibe JSON, delega, devuelve JSON
     │
     ▼
[ApplicationService]  application/     → orquesta casos de uso (transacciones)
     │
     ▼
[Service]             service/         → lógica de negocio específica
     │
     ▼
[Repository]          repository/      → acceso a la base de datos (JPA)
     │
     ▼
[Model / DTO]         model/ + dto/    → entidades JPA y objetos de transferencia
```

---

## Flujo de una petición típica (ejemplo: agregar al carrito)

```
POST /api/v1/cart/items
Authorization: Bearer <token>

1. CartController.addItem()
   └── extrae el token del header Authorization

2. CurrentSessionResolver.resolveCurrentToken()
   └── BearerTokenExtractor → limpia "Bearer " del header

3. CartApplicationService.addItem()
   └── userSessionService.requireActiveSession(token) → busca la sesión en DB
   └── si session.user != null → carrito de usuario registrado
       si session.user == null → carrito de invitado (por sessionId)
   └── cartService.addItem(cartId, productId, quantity)

4. WebResponseMapper.toCartResponse() → convierte DTO a respuesta JSON
```

---

## Seguridad y sesiones

| Mecanismo | Dónde |
|-----------|-------|
| Token tipo Bearer (UUID) | `BearerTokenExtractor` extrae del header `Authorization` |
| Sesión de invitado | `POST /api/v1/auth/guest-session` → devuelve token sin usuario |
| Login/Registro | `POST /api/v1/auth/login` o `/register` → devuelve token con usuario |
| Guard de admin | `AdminAccessGuard.requireAdmin()` → verifica rol en DB antes de cada operación admin |
| Contraseñas | `BCryptPasswordHasher` → hashing con BCrypt |

---

## Manejo de errores

El flujo de errores está centralizado en tres clases:

```
Excepción lanzada desde cualquier capa
         │
         ▼
ApiExceptionHandler   (@RestControllerAdvice)
  ├── MethodArgumentNotValidException  → 400 (campos inválidos en @Valid)
  ├── ConstraintViolationException     → 400
  ├── HttpMessageNotReadableException  → 400 (JSON malformado)
  ├── AuthenticationException          → 401
  ├── AuthorizationException           → 403
  ├── BusinessException                → varía (404, 409, etc.)
  └── Exception (catch-all)            → 500
         │
         ▼
DomainExceptionMapper   → convierte excepción a ApiErrorCode enum
         │
         ▼
ErrorResponseFactory    → construye ApiErrorResponse { code, message, fieldErrors, timestamp, path }
```

### Excepciones de dominio disponibles

| Excepción | Código API | HTTP |
|-----------|-----------|------|
| `EntityNotFoundException` | `RESOURCE_NOT_FOUND` | 404 |
| `AuthenticationException` | `UNAUTHORIZED` | 401 |
| `AuthorizationException` | `FORBIDDEN` | 403 |
| `DuplicateEntityException` | `DUPLICATE_RESOURCE` | 409 |
| `InsufficientStockException` | `INSUFFICIENT_STOCK` | 409 |
| `InvalidCartStateException` | `INVALID_CART_STATE` | 409 |
| `CartMergeException` | `CART_MERGE_ERROR` | 409 |
| `ValidationException` | `VALIDATION_ERROR` | 400 |

---

## Cómo interactúa con el frontend

En `WebConfig.java` se configura CORS para permitir llamadas desde el frontend:

```java
// Lee la propiedad app.cors.allowed-origins del application.properties
registry.addMapping("/**")
    .allowedOrigins(origins)           // ej: http://localhost:3000
    .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true)
    .maxAge(3600);
```

### Endpoints disponibles para el frontend (`/api/v1`)

| Grupo | Ruta | Quién puede |
|-------|------|------------|
| Auth | `/auth/*` | Todos |
| Catálogo | `/products`, `/categories` | Todos |
| Carrito | `/cart/*` | Con token (invitado o usuario) |
| Órdenes | `/orders/*` | Solo usuario registrado |
| Mi perfil | `/users/me/*` | Solo usuario registrado |
| Admin | `/admin/products`, `/admin/users` | Solo ADMIN |

El frontend siempre envía `Authorization: Bearer <token>` en el header. El token se obtiene al hacer login, registro o al crear una sesión de invitado, y se persiste en la tabla `UserSession` de la base de datos.