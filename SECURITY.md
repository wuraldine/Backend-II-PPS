# Guía de Seguridad - Product Purchasing System

## 🔒 Seguridad de Credenciales y Datos Sensibles

Este documento describe las medidas de seguridad implementadas para proteger información sensible.

---

## ✅ Medidas Implementadas

### 1. Variables de Entorno

**Todas las credenciales se manejan mediante variables de entorno:**

```bash
# Archivo .env (NO en Git)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=pps_db
DB_USER=user_pps
DB_PASSWORD=User@2026!  # ⚠️ Cambiar por contraseña real segura
```

**Archivos que usan variables de entorno:**
- ✅ `DatabaseConfig.java` - Lee de `System.getenv()`
- ✅ `persistence.xml` - Usa `${DB_USER}` y `${DB_PASSWORD}`
- ✅ `.env` - Archivo local (ignorado por Git)

---

### 2. Archivo .gitignore

**Archivos sensibles excluidos de Git:**

```gitignore
### Environment & Logs ###
.env              # ← Archivo con credenciales reales
*.env             # ← Cualquier variante de .env
!.env.example     # ← Permitir .env.example (sin credenciales)
*.log             # ← Logs pueden contener info sensible
```

**Verificación:**
```bash
# Verificar que .env NO esté en Git
git ls-files | grep ".env$"
# (No debe retornar nada)

# Verificar que .env.example SÍ esté en Git
git ls-files | grep ".env.example"
# .env.example
```

---

### 3. Archivo .env.example

**Template SIN credenciales reales:**

```properties
# ⚠️ IMPORTANTE: Esta es una contraseña de EJEMPLO
# ⚠️ DEBE ser cambiada por una contraseña segura real
# ⚠️ NO usar esta contraseña en producción

DB_USER=user_pps
DB_PASSWORD=YOUR_SECURE_PASSWORD_HERE
```

**Propósito:**
- Mostrar la estructura de variables necesarias
- NO contiene credenciales reales
- Sirve como template para crear `.env` local

---

### 4. Scripts SQL

**`schema.sql` NO contiene contraseñas hardcodeadas:**

```sql
-- ❌ MAL (antes):
CREATE USER 'user_pps'@'localhost' IDENTIFIED BY 'User@2026!';

-- ✅ BIEN (ahora):
CREATE USER 'user_pps'@'localhost' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';
```

**Uso correcto:**
```bash
# El usuario debe proporcionar la contraseña al ejecutar
mysql -u root -p < schema.sql
# Se solicitará contraseña de root

# O usar variable de entorno
DB_PASSWORD='MiPasswordSegura!' mysql -u root -p < schema.sql
```

---

### 5. Valores por Defecto Seguros

**DatabaseConfig.java:**

```java
// ❌ MAL:
private static final String DEFAULT_DB_USER = "root";

// ✅ BIEN:
private static final String DEFAULT_DB_USER = "user_pps"; // NO usar root
```

**persistence.xml:**

```xml
<!-- ❌ MAL: -->
<property name="jakarta.persistence.jdbc.user" value="${DB_USER:root}"/>

<!-- ✅ BIEN: -->
<property name="jakarta.persistence.jdbc.user" value="${DB_USER:user_pps}"/>
```

---

## 🚨 Qué NO Hacer

### ❌ NO Hardcodear Credenciales

```java
// ❌ NUNCA hacer esto:
String password = "User@2026!";
String dbUrl = "jdbc:mysql://localhost:3306/pps_db?user=root&password=secret123";

// ✅ Siempre usar variables de entorno:
String password = System.getenv("DB_PASSWORD");
String dbUrl = DatabaseConfig.getJdbcUrl();
```

---

### ❌ NO Subir .env a Git

```bash
# ❌ NUNCA hacer esto:
git add .env
git commit -m "Add env file"

# ✅ Verificar que esté ignorado:
git status
# .env NO debe aparecer en la lista
```

---

### ❌ NO Poner Contraseñas en Comentarios

```java
// ❌ MAL:
// Password: User@2026!
// DB_PASSWORD=secret123

// ✅ BIEN:
// Password debe configurarse en archivo .env
```

---

## ✅ Checklist de Seguridad

### Antes de Hacer Commit:

- [ ] **Verificar que .env NO esté staged**
  ```bash
  git status
  # .env NO debe aparecer
  ```

- [ ] **Buscar credenciales hardcodeadas**
  ```bash
  grep -r "password.*=.*['\"]" src/
  grep -r "jdbc:mysql://.*:.*@" src/
  ```

- [ ] **Verificar .gitignore**
  ```bash
  cat .gitignore | grep -E "\.env$|^\*\.env"
  ```

- [ ] **Verificar valores por defecto**
  ```bash
  grep -r "DEFAULT_DB_USER.*root" src/
  # NO debe retornar nada
  ```

---

## 🔐 Política de Contraseñas MySQL

**Requisitos mínimos:**
- ✅ Mínimo 8 caracteres
- ✅ Al menos una mayúscula (A-Z)
- ✅ Al menos una minúscula (a-z)
- ✅ Al menos un número (0-9)
- ✅ Al menos un carácter especial (@, !, #, $, %, etc.)

**Ejemplos válidos:**
- `MySecure@Pass2026!`
- `Db#Admin2026$`
- `Product!System2026`

**Ejemplos NO válidos:**
- `password` (sin mayúsculas, números, símbolos)
- `Password123` (sin símbolos especiales)
- `Pass@1` (menos de 8 caracteres)

---

## 📝 Configuración por Ambiente

### Desarrollo (Local)

```properties
# .env (local)
DB_USER=user_pps
DB_PASSWORD=User@2026!  # Contraseña de desarrollo
DB_HOST=localhost
DB_PORT=3306
DB_DDL_AUTO=update
DB_SHOW_SQL=true
LOG_LEVEL=DEBUG
```

### Staging

```properties
# Variables de entorno en servidor
DB_USER=user_pps_staging
DB_PASSWORD=<contraseña-segura-staging>
DB_HOST=staging-db.example.com
DB_PORT=3306
DB_DDL_AUTO=validate
DB_SHOW_SQL=false
LOG_LEVEL=INFO
```

### Producción

```properties
# Variables de entorno en servidor
DB_USER=user_pps_prod
DB_PASSWORD=<contraseña-muy-segura-producción>
DB_HOST=prod-db.example.com
DB_PORT=3306
DB_DDL_AUTO=none
DB_SHOW_SQL=false
LOG_LEVEL=WARN
```

---

## 🛡️ Buenas Prácticas Adicionales

### 1. Rotación de Contraseñas

```bash
# Cambiar contraseña periódicamente
mysql -u root -p -e "ALTER USER 'user_pps'@'localhost' IDENTIFIED BY 'NuevaPassword@2026!';"
```

### 2. Principio de Mínimo Privilegio

```sql
-- ✅ Solo dar permisos necesarios
GRANT SELECT, INSERT, UPDATE, DELETE ON pps_db.* TO 'user_pps'@'localhost';

-- ❌ NO dar todos los privilegios en producción
-- GRANT ALL PRIVILEGES ON *.* TO 'user_pps'@'localhost';
```

### 3. Separar Usuarios por Ambiente

```sql
-- Desarrollo
CREATE USER 'user_pps_dev'@'localhost' IDENTIFIED BY '...';

-- Staging
CREATE USER 'user_pps_staging'@'staging-host' IDENTIFIED BY '...';

-- Producción
CREATE USER 'user_pps_prod'@'prod-host' IDENTIFIED BY '...';
```

### 4. Usar Gestores de Secretos (Futuro)

Para producción, considerar:
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault
- Google Secret Manager

---

## 📞 Contacto de Seguridad

Si encuentras alguna vulnerabilidad de seguridad, repórtala a:
- **Email**: security@pps.com
- **No publicar** vulnerabilidades en GitHub Issues

---

## 📚 Referencias

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [MySQL Security Best Practices](https://dev.mysql.com/doc/refman/8.0/en/security.html)
- [12-Factor App - Config](https://12factor.net/config)

---

**Última actualización:** 3 de febrero de 2026  
**Versión:** 1.0  
**Autor:** Luis Goenaga
