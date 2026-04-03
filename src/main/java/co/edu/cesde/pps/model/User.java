package co.edu.cesde.pps.model;

import co.edu.cesde.pps.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad User - Representa un usuario registrado del sistema.
 *
 * El usuario debe registrarse para completar el checkout y crear órdenes.
 *
 * Campos:
 * - userId: Identificador único del usuario (PK)
 * - roleId: Rol del usuario (FK a Role) - determina permisos
 * - email: Email único del usuario (UNIQUE) - usado para login
 * - passwordHash: Hash de la contraseña (nunca texto plano)
 * - firstName: Nombre del usuario
 * - lastName: Apellido del usuario
 * - phone: Teléfono de contacto
 * - status: Estado del usuario (ACTIVE, INACTIVE, BLOCKED)
 * - createdAt: Fecha de creación de la cuenta
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con Role
 * - 1:N con Address (direcciones del usuario)
 * - 1:N con UserSession (sesiones activas/históricas)
 * - 1:N con Cart (carritos del usuario)
 * - 1:N con Order (órdenes del usuario)
 */
public class User {

    private Long userId;
    private Long roleId;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private LocalDateTime createdAt;

    // Constructor vacío (requerido para JPA futuro)
    public User() {
    }

    // Constructor con campos obligatorios
    public User(Long roleId, String email, String passwordHash, String firstName, String lastName) {
        this.roleId = roleId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = UserStatus.ACTIVE; // Por defecto activo
        this.createdAt = LocalDateTime.now();
    }

    // Constructor completo (excepto ID y timestamp autogenerados)
    public User(Long roleId, String email, String passwordHash, String firstName, String lastName,
                String phone, UserStatus status) {
        this.roleId = roleId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
