package co.edu.cesde.pps.security;

/**
 * Abstracción para hashing y verificación de contraseñas.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
