package co.edu.cesde.pps.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad OrderItem - Detalle de productos comprados en una orden.
 *
 * Representa un producto incluido en una orden con su cantidad y precio histórico.
 *
 * Campos:
 * - orderItemId: Identificador único del item (PK)
 * - orderId: Orden a la que pertenece (FK a Order)
 * - productId: Producto comprado (FK a Product)
 * - quantity: Cantidad comprada
 * - unitPrice: Precio unitario al momento de la compra (histórico)
 * - lineTotal: Total de la línea (unitPrice * quantity)
 *
 * Restricción UNIQUE (orderId, productId):
 * Un producto no puede aparecer duplicado en la misma orden. Si el usuario
 * compra el mismo producto dos veces en checkout, debe consolidarse en un
 * solo OrderItem con cantidad sumada.
 *
 * Congelación de precio (unitPrice):
 * Se guarda el precio del producto en el momento de crear la orden.
 * Esto es crucial para auditoría y reportes históricos, ya que los precios
 * de productos pueden cambiar con el tiempo.
 *
 * lineTotal:
 * Se puede calcular (unitPrice * quantity) o guardar para optimización.
 * Guardarlo facilita consultas y reportes sin recalcular.
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con Order (un item pertenece a una orden)
 * - N:1 con Product (un item referencia a un producto)
 */
public class OrderItem {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Constructor vacío (requerido para JPA futuro)
    public OrderItem() {
    }

    // Constructor con campos obligatorios (lineTotal se calcula)
    public OrderItem(Long orderId, Long productId, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = calculateLineTotal();
    }

    // Constructor completo (excepto ID autogenerado)
    public OrderItem(Long orderId, Long productId, Integer quantity,
                     BigDecimal unitPrice, BigDecimal lineTotal) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal != null ? lineTotal : calculateLineTotal();
    }

    // Getters y Setters

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
        // Recalcular lineTotal al cambiar quantity
        this.lineTotal = calculateLineTotal();
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
        // Recalcular lineTotal al cambiar unitPrice
        this.lineTotal = calculateLineTotal();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        if (lineTotal != null && lineTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Line total cannot be negative");
        }
        this.lineTotal = lineTotal;
    }

    // Método helper para calcular total de la línea
    public BigDecimal calculateLineTotal() {
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
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
