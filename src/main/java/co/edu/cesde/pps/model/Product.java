package co.edu.cesde.pps.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Product - Representa productos vendibles en la tienda.
 *
 * Campos:
 * - productId: Identificador único del producto (PK)
 * - category: Categoría del producto (N:1 con Category)
 * - sku: Stock Keeping Unit (UNIQUE) - código único de inventario
 * - name: Nombre del producto
 * - description: Descripción detallada del producto
 * - price: Precio actual del producto (BigDecimal para precisión monetaria)
 * - stockQty: Cantidad en stock/inventario
 * - isActive: Indica si el producto está activo (visible en catálogo)
 * - createdAt: Fecha de creación del producto
 *
 * Consideraciones de diseño:
 * - price usa BigDecimal para evitar errores de redondeo en cálculos monetarios
 * - isActive permite ocultar productos sin borrarlos de la base de datos
 * - sku único facilita integración con sistemas de inventario externos
 *
 * Relaciones:
 * - N:1 con Category (muchos productos pertenecen a una categoría)
 * - 1:N con CartItem (un producto puede estar en múltiples carritos)
 * - 1:N con OrderItem (un producto puede estar en múltiples órdenes)
 */
public class Product {

    private Long productId;
    private Category category;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQty;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Constructor vacío (requerido para JPA futuro)
    public Product() {
    }

    // Constructor con campos obligatorios
    public Product(Category category, String sku, String name, BigDecimal price, Integer stockQty) {
        this.category = category;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
        this.isActive = true; // Por defecto activo
        this.createdAt = LocalDateTime.now();
    }

    // Constructor completo (excepto ID y timestamp autogenerados)
    public Product(Category category, String sku, String name, String description,
                   BigDecimal price, Integer stockQty, Boolean isActive) {
        this.category = category;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQty = stockQty;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        // Validación básica: precio no puede ser negativo
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public void setStockQty(Integer stockQty) {
        // Validación básica: stock no puede ser negativo
        if (stockQty != null && stockQty < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.stockQty = stockQty;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Método helper para verificar disponibilidad
    public boolean isAvailable() {
        return isActive != null && isActive && stockQty != null && stockQty > 0;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", categoryId=" + (category != null ? category.getCategoryId() : null) +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQty=" + stockQty +
                ", isActive=" + isActive +
                ", isAvailable=" + isAvailable() +
                ", createdAt=" + createdAt +
                '}';
    }
}
