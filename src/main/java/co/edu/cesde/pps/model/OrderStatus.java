package co.edu.cesde.pps.model;

import java.util.Objects;

/**
 * Entidad OrderStatus - Catálogo de estados posibles de una orden.
 *
 * Ejemplos: pending, paid, shipped, delivered, cancelled
 *
 * Campos:
 * - orderStatusId: Identificador único del estado (PK)
 * - name: Nombre único del estado (UNIQUE)
 *
 * Relaciones (futuro - etapa02):
 * - 1:N con Order (un estado puede aplicar a múltiples órdenes)
 */
public class OrderStatus {

    private Long orderStatusId;
    private String name;

    // Constructor vacío (requerido para JPA futuro)
    public OrderStatus() {
    }

    // Constructor con campos obligatorios
    public OrderStatus(String name) {
        this.name = name;
    }

    // Getters y Setters

    public Long getOrderStatusId() {
        return orderStatusId;
    }

    public void setOrderStatusId(Long orderStatusId) {
        this.orderStatusId = orderStatusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatus that = (OrderStatus) o;
        return Objects.equals(orderStatusId, that.orderStatusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderStatusId);
    }

    // toString sin navegación a objetos relacionados

    @Override
    public String toString() {
        return "OrderStatus{" +
                "orderStatusId=" + orderStatusId +
                ", name='" + name + '\'' +
                '}';
    }
}
