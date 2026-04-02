package co.edu.cesde.pps.model;

import java.util.Objects;

/**
 * Entidad PaymentMethod - Catálogo de métodos de pago disponibles.
 *
 * Ejemplos: credit_card, bank_transfer, cash_on_delivery, paypal
 *
 * Campos:
 * - paymentMethodId: Identificador único del método (PK)
 * - name: Nombre único del método (UNIQUE)
 *
 * Relaciones (futuro - etapa02):
 * - 1:N con Payment (un método puede usarse en múltiples pagos)
 */
public class PaymentMethod {

    private Long paymentMethodId;
    private String name;

    // Constructor vacío (requerido para JPA futuro)
    public PaymentMethod() {
    }

    // Constructor con campos obligatorios
    public PaymentMethod(String name) {
        this.name = name;
    }

    // Getters y Setters

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
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
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(paymentMethodId, that.paymentMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentMethodId);
    }

    // toString sin navegación a objetos relacionados

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "paymentMethodId=" + paymentMethodId +
                ", name='" + name + '\'' +
                '}';
    }
}
