package co.edu.cesde.pps.model;

import co.edu.cesde.pps.util.ValidationUtils;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "sku", nullable = false, unique = true, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image", length = 1000)
    private String image;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_qty", nullable = false)
    private Integer stockQty;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setPrice(BigDecimal price) {
        ValidationUtils.validateNonNegative(price, "price");
        this.price = price;
    }

    public void setStockQty(Integer stockQty) {
        ValidationUtils.validateNonNegative(stockQty, "stockQty");
        this.stockQty = stockQty;
    }

    public boolean isAvailable() {
        return isActive != null && isActive && stockQty != null && stockQty > 0;
    }

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

    @Override
    public String toString() {
        return "Product{productId=" + productId +
                ", categoryId=" + (category != null ? category.getCategoryId() : null) +
                ", sku='" + sku + "', name='" + name + "', image='" + image +
                "', price=" + price + ", stockQty=" + stockQty +
                ", isActive=" + isActive + ", isAvailable=" + isAvailable() +
                ", createdAt=" + createdAt + "}";
    }
}
