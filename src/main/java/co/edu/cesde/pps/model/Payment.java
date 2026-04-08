package co.edu.cesde.pps.model;

import co.edu.cesde.pps.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad Payment - Registra transacciones de pago asociadas a una orden.
 *
 * Una orden puede tener múltiples pagos (reintentos, pagos parciales, reembolsos).
 *
 * Campos:
 * - paymentId: Identificador único del pago (PK)
 * - orderId: Orden asociada (FK a Order)
 * - paymentMethodId: Método de pago usado (FK a PaymentMethod)
 * - paymentStatusId: Estado del pago (FK a PaymentStatus)
 * - amount: Monto del pago (BigDecimal para precisión)
 * - currency: Moneda del pago (USD, COP, EUR)
 * - providerReference: Referencia del proveedor de pagos (ej: ID de transacción de pasarela)
 * - paidAt: Fecha/hora en que se completó el pago exitosamente
 *
 * Consideraciones de diseño:
 * - Múltiples pagos por orden permiten manejar:
 *   * Reintentos de pago fallido
 *   * Pagos parciales
 *   * Reembolsos (amount negativo)
 * - providerReference crucial para conciliación con pasarelas de pago externas
 * - paidAt puede ser NULL si el pago aún no se ha completado (pending)
 * - Currency enum permite soportar múltiples monedas
 * - BigDecimal en amount para precisión monetaria
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con Order (un pago pertenece a una orden)
 * - N:1 con PaymentMethod (método usado)
 * - N:1 con PaymentStatus (estado actual)
 */
public class Payment {

    private Long paymentId;
    private Long orderId;
    private Long paymentMethodId;
    private Long paymentStatusId;
    private BigDecimal amount;
    private Currency currency;
    private String providerReference;
    private LocalDateTime paidAt;

    // Constructor vacío (requerido para JPA futuro)
    public Payment() {
    }

    // Constructor con campos obligatorios (paidAt NULL para pending)
    public Payment(Long orderId, Long paymentMethodId, Long paymentStatusId,
                   BigDecimal amount, Currency currency) {
        this.orderId = orderId;
        this.paymentMethodId = paymentMethodId;
        this.paymentStatusId = paymentStatusId;
        this.amount = amount;
        this.currency = currency;
        this.paidAt = null; // Se establece cuando el pago se completa
    }

    // Constructor completo (excepto ID autogenerado)
    public Payment(Long orderId, Long paymentMethodId, Long paymentStatusId,
                   BigDecimal amount, Currency currency, String providerReference, LocalDateTime paidAt) {
        this.orderId = orderId;
        this.paymentMethodId = paymentMethodId;
        this.paymentStatusId = paymentStatusId;
        this.amount = amount;
        this.currency = currency;
        this.providerReference = providerReference;
        this.paidAt = paidAt;
    }

    // Getters y Setters

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getPaymentStatusId() {
        return paymentStatusId;
    }

    public void setPaymentStatusId(Long paymentStatusId) {
        this.paymentStatusId = paymentStatusId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        // Validación: amount puede ser negativo (reembolsos), pero no null
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    // Método helper para verificar si el pago está completado
    public boolean isPaid() {
        return paidAt != null;
    }

    // Método helper para verificar si es un reembolso
    public boolean isRefund() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", paymentMethodId=" + paymentMethodId +
                ", paymentStatusId=" + paymentStatusId +
                ", amount=" + amount +
                ", currency=" + currency +
                ", providerReference='" + providerReference + '\'' +
                ", paidAt=" + paidAt +
                ", isPaid=" + isPaid() +
                ", isRefund=" + isRefund() +
                '}';
    }
}
