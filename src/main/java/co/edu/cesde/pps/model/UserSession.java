package co.edu.cesde.pps.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad UserSession - Representa sesiones activas para navegación.
 *
 * Crucial para manejar carritos de invitado y sesiones de usuarios registrados.
 *
 * Campos:
 * - sessionId: Identificador único de la sesión (PK)
 * - userId: Usuario asociado (FK a User) - NULLABLE para invitados
 * - sessionToken: Token único de sesión (UNIQUE) - mapea con cookie/JWT
 * - createdAt: Fecha de creación de la sesión
 * - expiresAt: Fecha de expiración de la sesión
 *
 * Comportamiento:
 * - userId = NULL → sesión de invitado (guest)
 * - userId = <id> → sesión de usuario registrado
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con User (opcional, nullable)
 * - 1:N con Cart (una sesión puede tener múltiples carritos en el tiempo)
 */
public class UserSession {

    private Long sessionId;
    private Long userId; // Nullable - NULL para invitados
    private String sessionToken;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Constructor vacío (requerido para JPA futuro)
    public UserSession() {
    }

    // Constructor para sesión de invitado (sin userId)
    public UserSession(String sessionToken, LocalDateTime expiresAt) {
        this.userId = null; // Invitado
        this.sessionToken = sessionToken;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Constructor para sesión de usuario registrado
    public UserSession(Long userId, String sessionToken, LocalDateTime expiresAt) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Constructor completo (excepto ID y createdAt autogenerados)
    public UserSession(Long userId, String sessionToken, LocalDateTime expiresAt, LocalDateTime createdAt) {
        this.userId = userId;
        this.sessionToken = sessionToken;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    // Getters y Setters

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    // Método helper para verificar si es sesión de invitado
    public boolean isGuestSession() {
        return userId == null;
    }

    // Método helper para verificar si la sesión ha expirado
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "UserSession{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", sessionToken='" + sessionToken + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", isGuest=" + isGuestSession() +
                ", isExpired=" + isExpired() +
                '}';
    }
}
