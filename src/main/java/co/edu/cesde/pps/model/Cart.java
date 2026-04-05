package co.edu.cesde.pps.model;

import co.edu.cesde.pps.enums.CartStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Cart - Contenedor del carrito de compras.
 *
 * El carrito puede pertenecer a un usuario registrado o a un invitado (guest).
 * Siempre está asociado a una sesión mediante sessionId.
 *
 * Campos:
 * - cartId: Identificador único del carrito (PK)
 * - userId: Usuario propietario (FK a User) - NULLABLE para carritos de invitado
 * - sessionId: Sesión asociada (FK a UserSession) - siempre requerido
 * - status: Estado del carrito (OPEN, CONVERTED, ABANDONED)
 * - createdAt: Fecha de creación del carrito
 * - updatedAt: Fecha de última actualización
 *
 * Comportamiento por tipo de usuario:
 * - Invitado: userId = NULL, sessionId = <session_id>
 * - Registrado: userId = <user_id>, sessionId = <session_id>
 *
 * Estados del carrito:
 * - OPEN: Carrito activo, usuario puede agregar/quitar items
 * - CONVERTED: Carrito convertido en orden (checkout completado)
 * - ABANDONED: Carrito abandonado o resultado de merge
 *
 * POLÍTICA DE CART MERGE (OBLIGATORIA):
 * =====================================
 * Cuando un usuario invitado se registra o inicia sesión y ya existe un carrito
 * abierto del usuario, se debe ejecutar el siguiente proceso de fusión (merge):
 *
 * Escenario:
 * - Carrito A: carrito del invitado (userId = NULL, status = OPEN)
 * - Carrito B: carrito del usuario registrado (userId = X, status = OPEN)
 *
 * Proceso de Merge (implementar en capa de servicio - etapa 3):
 * 1. Identificar ambos carritos por sessionId y userId
 * 2. Para cada CartItem del carrito invitado (A):
 *    a. Si el mismo productId existe en carrito usuario (B):
 *       - Sumar las cantidades (quantity)
 *       - Resolver conflicto de unitPrice (conservar más reciente o del usuario según política)
 *    b. Si el productId NO existe en carrito usuario (B):
 *       - Mover/copiar el CartItem al carrito del usuario (B)
 * 3. Marcar carrito invitado (A) como status = ABANDONED
 * 4. Usuario continúa con carrito único (B) sin pérdida de productos
 *
 * Resultado:
 * - El usuario mantiene un solo carrito activo
 * - No se pierden productos agregados como invitado
 * - No hay duplicación innecesaria de items
 *
 * Ver documentación completa en: documents_external/er_model_documentation.md - Sección 5
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con User (opcional, nullable para invitados)
 * - N:1 con UserSession (obligatorio)
 * - 1:N con CartItem (items del carrito)
 */
public class Cart {

    private Long cartId;
    private Long userId; // Nullable - NULL para invitados
    private Long sessionId;
    private CartStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor vacío (requerido para JPA futuro)
    public Cart() {
    }

    // Constructor para carrito de invitado
    public Cart(Long sessionId) {
        this.userId = null; // Invitado
        this.sessionId = sessionId;
        this.status = CartStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor para carrito de usuario registrado
    public Cart(Long userId, Long sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.status = CartStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor completo (excepto ID y timestamps autogenerados)
    public Cart(Long userId, Long sessionId, CartStatus status) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.status = status != null ? status : CartStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public CartStatus getStatus() {
        return status;
    }

    public void setStatus(CartStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Método helper para actualizar timestamp de modificación
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // Método helper para verificar si es carrito de invitado
    public boolean isGuestCart() {
        return userId == null;
    }

    // Método helper para verificar si el carrito está activo
    public boolean isOpen() {
        return status == CartStatus.OPEN;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(cartId, cart.cartId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId);
    }

    // toString sin navegación a objetos relacionados (solo IDs y tamaño de colección)

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", userId=" + userId +
                ", sessionId=" + sessionId +
                ", status=" + status +
                ", isGuest=" + isGuestCart() +
                ", isOpen=" + isOpen() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
