package co.edu.cesde.pps.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad CartItem - Detalle de items en el carrito de compras.
 *
 * Representa un producto agregado al carrito con su cantidad y precio congelado.
 *
 * Campos:
 * - cartItemId: Identificador único del item (PK)
 * - cartId: Carrito al que pertenece (FK a Cart)
 * - productId: Producto agregado (FK a Product)
 * - quantity: Cantidad del producto en el carrito
 * - unitPrice: Precio unitario congelado al agregar (BigDecimal para precisión)
 * - addedAt: Fecha en que se agregó el item al carrito
 *
 * Restricción UNIQUE (cartId, productId):
 * Un producto no puede aparecer duplicado en el mismo carrito. Si se agrega
 * el mismo producto dos veces, se debe actualizar la cantidad del item existente.
 *
 * Congelación de precio (unitPrice):
 * Se guarda el precio del producto en el momento de agregarlo al carrito.
 * Esto asegura consistencia si el precio del producto cambia mientras el
 * usuario navega. El precio se "congela" al agregar al carrito.
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con Cart (un item pertenece a un carrito)
 * - N:1 con Product (un item referencia a un producto)
 */
public class CartItem {

    private Long cartItemId;
    private Long cartId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private LocalDateTime addedAt;

    // Constructor vacío (requerido para JPA futuro)
    public CartItem() {
    }

    // Constructor con campos obligatorios
    public CartItem(Long cartId, Long productId, Integer quantity, BigDecimal unitPrice) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.addedAt = LocalDateTime.now();
    }

    // Constructor completo (excepto ID y timestamp autogenerado)
    public CartItem(Long cartId, Long productId, Integer quantity, BigDecimal unitPrice, LocalDateTime addedAt) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.addedAt = addedAt != null ? addedAt : LocalDateTime.now();
    }

    // Getters y Setters

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        // Validación básica: cantidad debe ser mayor a 0
        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        // Validación básica: precio no puede ser negativo
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = unitPrice;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    // Método helper para calcular subtotal del item
    public BigDecimal calculateSubtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(cartItemId, cartItem.cartItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "CartItem{" +
                "cartItemId=" + cartItemId +
                ", cartId=" + cartId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + calculateSubtotal() +
                ", addedAt=" + addedAt +
                '}';
    }
}
