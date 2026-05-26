package co.edu.cesde.pps.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Configuration
public class DataInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(DataInitializerConfig.class);

    // Contraseñas para el entorno de desarrollo
    private static final String[][] USER_PASSWORDS = {
        {"1", "admin123"},
        {"2", "customer123"},
        {"3", "customer123"},
        {"4", "manager123"}
    };

    @Bean
    public CommandLineRunner seedDatabase(DataSource dataSource) {
        return args -> {
            log.info("Ejecutando data.sql para poblar la base de datos...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("sql/data.sql"));
            populator.setContinueOnError(true);
            populator.setSeparator(";");
            populator.execute(dataSource);
            log.info("data.sql ejecutado correctamente.");

            hashUserPasswords(dataSource);
        };
    }

    private void hashUserPasswords(DataSource dataSource) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        try (Connection conn = dataSource.getConnection()) {
            for (String[] entry : USER_PASSWORDS) {
                String hash = encoder.encode(entry[1]);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET password_hash = ? WHERE user_id = ?")) {
                    ps.setString(1, hash);
                    ps.setInt(2, Integer.parseInt(entry[0]));
                    ps.executeUpdate();
                }
            }
            log.info("Contraseñas de usuarios configuradas con BCrypt.");
        } catch (Exception e) {
            log.warn("No se pudieron hashear las contraseñas: {}", e.getMessage());
        }
    }
}
