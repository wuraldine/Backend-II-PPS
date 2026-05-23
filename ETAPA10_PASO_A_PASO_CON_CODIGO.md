# ETAPA 10 - Paso a paso con código (Spring Boot + Spring Data JPA)

> **Nota:** este archivo es de apoyo local para estudiantes.
> No hace parte del flujo de commits y no es necesario subirlo a git.

## Objetivo
Pasar el proyecto desde `etapa09` a una aplicación **Spring real** usando:

- `Spring Boot`
- `Spring Data JPA`
- `@Service`
- `@Transactional`
- `JpaRepository`

En esta etapa **todavía no se crean endpoints**.
La meta es reemplazar la persistencia en memoria por persistencia real con base de datos.

---

## Qué cambia realmente en ETAPA 10
En `etapa09` ya existían:

- entidades con anotaciones JPA
- relaciones reales entre entidades
- prevención de ciclos con `@JsonManagedReference` / `@JsonBackReference`

Pero los `service` seguían trabajando con:

- listas `inMemory`
- métodos `generateNextId()`
- catálogos fake creados dentro del código

En `etapa10` se cambia eso por:

- arranque real con Spring Boot
- configuración con `application.yml`
- repositorios `JpaRepository`
- services con `@Service` y `@Transactional`
- entidades persistidas en base de datos

---

# 1. Reemplazar `pom.xml`

## Archivo
`pom.xml`

## Qué debes hacer
Reemplaza el contenido por este:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>

    <groupId>co.edu.cesde</groupId>
    <artifactId>product-purchasing-system</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

## Por qué se cambia
Porque `JpaRepository` necesita el ecosistema Spring Boot para:

- escanear componentes
- crear beans
- inyectar dependencias
- configurar JPA automáticamente

---

# 2. Crear la clase de arranque

## Archivo nuevo
`src/main/java/co/edu/cesde/pps/PpsApplication.java`

```java
package co.edu.cesde.pps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PpsApplication.class, args);
    }
}
```

## Por qué se crea
Esta clase convierte el proyecto en una aplicación Spring Boot real.

---

# 3. Crear la configuración principal

## Archivo nuevo
`src/main/resources/application.yml`

```yaml
spring:
  application:
    name: product-purchasing-system
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:pps_db}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USER:user_pps}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: ${DB_DDL_AUTO:update}
    open-in-view: false
    show-sql: ${DB_SHOW_SQL:true}
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  sql:
    init:
      mode: never

logging:
  level:
    co.edu.cesde.pps: DEBUG
    org.hibernate.SQL: ${LOG_SQL_LEVEL:DEBUG}
    org.hibernate.orm.jdbc.bind: ${LOG_SQL_BIND_LEVEL:TRACE}
```

## Qué cambia aquí
Ahora la configuración de BD y JPA deja de estar en clases utilitarias manuales y pasa al modelo estándar de Spring Boot.

---

# 4. Crear el package `repository`

## Carpeta nueva
`src/main/java/co/edu/cesde/pps/repository`

Crea las siguientes interfaces.

---

## 4.1 `UserRepository.java`
`src/main/java/co/edu/cesde/pps/repository/UserRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
```

---

## 4.2 `RoleRepository.java`
`src/main/java/co/edu/cesde/pps/repository/RoleRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNameIgnoreCase(String name);
}
```

---

## 4.3 `AddressRepository.java`
`src/main/java/co/edu/cesde/pps/repository/AddressRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    long countByUser_UserId(Long userId);

    List<Address> findByUser_UserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Address a set a.isDefault = false where a.user.userId = :userId")
    void unsetDefaultByUserId(@Param("userId") Long userId);
}
```

---

## 4.4 `CategoryRepository.java`
`src/main/java/co/edu/cesde/pps/repository/CategoryRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsBySlugIgnoreCase(String slug);

    Optional<Category> findBySlugIgnoreCase(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParent_CategoryId(Long parentId);
}
```

---

## 4.5 `ProductRepository.java`
`src/main/java/co/edu/cesde/pps/repository/ProductRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    List<Product> findByIsActiveTrue();

    List<Product> findByCategory_CategoryId(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);
}
```

---

## 4.6 `CartRepository.java`
`src/main/java/co/edu/cesde/pps/repository/CartRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_UserIdAndStatus(Long userId, CartStatus status);
}
```

---

## 4.7 `OrderRepository.java`
`src/main/java/co/edu/cesde/pps/repository/OrderRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNumberIgnoreCase(String orderNumber);

    Optional<Order> findByOrderNumberIgnoreCase(String orderNumber);

    List<Order> findByUser_UserId(Long userId);

    List<Order> findByOrderStatus_OrderStatusId(Long statusId);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
```

---

## 4.8 `OrderStatusRepository.java`
`src/main/java/co/edu/cesde/pps/repository/OrderStatusRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {

    Optional<OrderStatus> findByNameIgnoreCase(String name);
}
```

---

## 4.9 `UserSessionRepository.java`
`src/main/java/co/edu/cesde/pps/repository/UserSessionRepository.java`

```java
package co.edu.cesde.pps.repository;

import co.edu.cesde.pps.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
```

---

# 5. Ajustar cascada en `Cart` y `Order`

## 5.1 Cambiar `Cart.items`
**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java`

Busca la colección `items` y déjala así:

```java
@OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference("cart-items")
@Builder.Default
private List<CartItem> items = new ArrayList<>();
```

## 5.2 Cambiar `Order.items`
**Archivo:** `src/main/java/co/edu/cesde/pps/model/Order.java`

Busca la colección `items` y déjala así:

```java
@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
@JsonManagedReference("order-items")
@Builder.Default
private List<OrderItem> items = new ArrayList<>();
```

## Por qué se cambia
Porque `CartItem` depende de `Cart` y `OrderItem` depende de `Order`.
Si no agregas cascada, al guardar el padre no se guardan correctamente los hijos nuevos.

---

# 6. Refactorizar `UserService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/UserService.java`

## Cambios importantes
- agregar `@Service`
- agregar `@Transactional(readOnly = true)`
- inyectar `UserRepository` y `RoleRepository`
- eliminar listas `inMemory`
- eliminar `generateNextId()`
- cargar el rol `CUSTOMER` desde BD

## Reemplaza la cabecera, atributos y constructor por esto

```java
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userMapper = new UserMapper();
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
```

## Reemplaza `registerUser(...)` por esto

```java
@Transactional
public UserDTO registerUser(String email, String passwordHash, String firstName,
                            String lastName, String phone) {
    ValidationUtils.validateEmail(email, "email");
    ValidationUtils.validateNotBlank(passwordHash, "passwordHash");
    ValidationUtils.validateMinLength(passwordHash, AppConfig.getMinPasswordLength(), "password");
    ValidationUtils.validateNotBlank(firstName, "firstName");
    ValidationUtils.validateNotBlank(lastName, "lastName");

    if (phone != null && !phone.isBlank()) {
        ValidationUtils.validatePhone(phone, "phone");
    }

    if (existsByEmail(email)) {
        throw new DuplicateEntityException("User", "email", email);
    }

    Role defaultRole = roleRepository.findByNameIgnoreCase("CUSTOMER")
            .orElseThrow(() -> new EntityNotFoundException("Role", "CUSTOMER"));

    User user = User.builder()
            .role(defaultRole)
            .email(email.toLowerCase().trim())
            .passwordHash(passwordHash)
            .firstName(firstName.trim())
            .lastName(lastName.trim())
            .phone(phone != null ? phone.trim() : null)
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

    user = userRepository.save(user);

    return userMapper.toDTO(user);
}
```

## Reemplaza estos métodos de consulta

```java
public UserDTO findByEmail(String email) {
    User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("User with email: " + email));

    return userMapper.toDTO(user);
}

public List<UserDTO> findAllUsers() {
    return userMapper.toDTOList(userRepository.findAll());
}

public boolean existsByEmail(String email) {
    return userRepository.existsByEmailIgnoreCase(email);
}

public User findUserEntityOrThrow(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
}
```

## Importante
Si dejas el `Role` fake creado con `new Role()`, el flujo ya no es consistente con persistencia real.

---

# 7. Refactorizar `AddressService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/AddressService.java`

## Reemplaza cabecera, atributos y constructor por esto

```java
@Service
@Transactional(readOnly = true)
public class AddressService {

    private final AddressMapper addressMapper;
    private final UserService userService;
    private final AddressRepository addressRepository;

    public AddressService(UserService userService, AddressRepository addressRepository) {
        this.addressMapper = new AddressMapper();
        this.userService = userService;
        this.addressRepository = addressRepository;
    }
```

## Reemplaza `addAddress(...)`

```java
@Transactional
public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
    User user = userService.findUserEntityOrThrow(userId);

    long currentCount = addressRepository.countByUser_UserId(userId);

    if (currentCount >= AppConfig.getMaxAddressesPerUser()) {
        throw new ValidationException("User has reached maximum number of addresses (" +
            AppConfig.getMaxAddressesPerUser() + ")");
    }

    validateAddressData(addressDTO);

    Address address = addressMapper.toEntity(addressDTO);

    user.getAddresses().add(address);
    address.setUser(user);

    if (currentCount == 0) {
        address.setIsDefault(true);
    } else if (Boolean.TRUE.equals(address.getIsDefault())) {
        unsetOtherDefaultAddresses(userId);
    }

    address = addressRepository.save(address);

    return addressMapper.toDTO(address);
}
```

## Reemplaza `updateAddress(...)`

```java
@Transactional
public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
    Address address = findAddressEntityOrThrow(addressId);

    validateAddressData(addressDTO);

    address.setType(addressDTO.getType());
    address.setLine1(addressDTO.getLine1());
    address.setLine2(addressDTO.getLine2());
    address.setCity(addressDTO.getCity());
    address.setState(addressDTO.getState());
    address.setCountry(addressDTO.getCountry());
    address.setPostalCode(addressDTO.getPostalCode());

    if (Boolean.TRUE.equals(addressDTO.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
        unsetOtherDefaultAddresses(address.getUser().getUserId());
        address.setIsDefault(true);
    }

    address = addressRepository.save(address);

    return addressMapper.toDTO(address);
}
```

## Reemplaza `deleteAddress(...)`

```java
@Transactional
public void deleteAddress(Long userId, Long addressId) {
    User user = userService.findUserEntityOrThrow(userId);
    Address address = findAddressEntityOrThrow(addressId);

    if (!address.getUser().getUserId().equals(userId)) {
        throw new ValidationException("Address does not belong to user");
    }

    user.getAddresses().remove(address);
    addressRepository.delete(address);

    if (Boolean.TRUE.equals(address.getIsDefault())) {
        List<Address> remainingAddresses = addressRepository.findByUser_UserId(userId);
        if (!remainingAddresses.isEmpty()) {
            Address newDefault = remainingAddresses.get(0);
            newDefault.setIsDefault(true);
            addressRepository.save(newDefault);
        }
    }
}
```

## Reemplaza utilitarios de consulta

```java
public List<AddressDTO> findUserAddresses(Long userId) {
    userService.findUserEntityOrThrow(userId);
    return addressMapper.toDTOList(addressRepository.findByUser_UserId(userId));
}

public Address findAddressEntityOrThrow(Long addressId) {
    return addressRepository.findById(addressId)
            .orElseThrow(() -> new EntityNotFoundException("Address", addressId));
}

private void unsetOtherDefaultAddresses(Long userId) {
    List<Address> addresses = addressRepository.findByUser_UserId(userId);
    addresses.forEach(a -> a.setIsDefault(false));
}
```

## Importante
**No hagas esto antes de eliminar:**

```java
address.setUser(null);
```

Porque `Address.user` es `nullable = false`.

---

# 8. Refactorizar `CategoryService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/CategoryService.java`

## Reemplaza cabecera, atributos y constructor

```java
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryMapper = new CategoryMapper();
        this.categoryRepository = categoryRepository;
    }
```

## Reemplaza `createCategory(...)`

```java
@Transactional
public CategoryDTO createCategory(CategoryDTO categoryDTO) {
    ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

    String slug = categoryDTO.getSlug();
    if (slug == null || slug.isBlank()) {
        slug = StringUtils.slugify(categoryDTO.getName());
    }

    if (existsBySlug(slug)) {
        throw new DuplicateEntityException("Category", "slug", slug);
    }

    Category category = categoryMapper.toEntity(categoryDTO);
    category.setSlug(slug);

    if (categoryDTO.getParentId() != null) {
        Category parent = findCategoryEntityOrThrow(categoryDTO.getParentId());
        category.setParent(parent);
        parent.getSubcategories().add(category);
    }

    category = categoryRepository.save(category);

    return categoryMapper.toDTO(category);
}
```

## Reemplaza `updateCategory(...)`

```java
@Transactional
public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
    Category category = findCategoryEntityOrThrow(categoryId);
    Category currentParent = category.getParent();

    ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

    String newSlug = categoryDTO.getSlug();
    if (newSlug == null || newSlug.isBlank()) {
        newSlug = StringUtils.slugify(categoryDTO.getName());
    }

    if (!category.getSlug().equals(newSlug) && existsBySlug(newSlug)) {
        throw new DuplicateEntityException("Category", "slug", newSlug);
    }

    category.setName(categoryDTO.getName());
    category.setSlug(newSlug);

    if (categoryDTO.getParentId() != null) {
        if (categoryDTO.getParentId().equals(categoryId)) {
            throw new ValidationException("Category cannot be its own parent");
        }

        Category newParent = findCategoryEntityOrThrow(categoryDTO.getParentId());

        if (wouldCreateCycle(category, newParent)) {
            throw new ValidationException("Cannot create cycle in category hierarchy");
        }

        if (currentParent != null && !currentParent.equals(newParent)) {
            currentParent.getSubcategories().remove(category);
        }
        if (!newParent.getSubcategories().contains(category)) {
            newParent.getSubcategories().add(category);
        }
        category.setParent(newParent);
    } else {
        if (currentParent != null) {
            currentParent.getSubcategories().remove(category);
        }
        category.setParent(null);
    }

    category = categoryRepository.save(category);

    return categoryMapper.toDTO(category);
}
```

## Reemplaza consultas base

```java
public CategoryDTO findBySlug(String slug) {
    Category category = categoryRepository.findBySlugIgnoreCase(slug)
            .orElseThrow(() -> new EntityNotFoundException("Category with slug: " + slug));

    return categoryMapper.toDTO(category);
}

public List<CategoryDTO> findAllCategories() {
    return categoryMapper.toDTOList(categoryRepository.findAll());
}

public List<CategoryDTO> findRootCategories() {
    return categoryMapper.toDTOList(categoryRepository.findByParentIsNull());
}

public List<CategoryDTO> findSubcategories(Long parentId) {
    findCategoryEntityOrThrow(parentId);
    return categoryMapper.toDTOList(categoryRepository.findByParent_CategoryId(parentId));
}

public boolean existsBySlug(String slug) {
    return categoryRepository.existsBySlugIgnoreCase(slug);
}

public Category findCategoryEntityOrThrow(Long categoryId) {
    return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category", categoryId));
}
```

---

# 9. Refactorizar `ProductService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/ProductService.java`

## Reemplaza cabecera, atributos y constructor

```java
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    public ProductService(CategoryService categoryService, ProductRepository productRepository) {
        this.productMapper = new ProductMapper();
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }
```

## Reemplaza `createProduct(...)`

```java
@Transactional
public ProductDTO createProduct(ProductDTO productDTO) {
    ValidationUtils.validateNotBlank(productDTO.getSku(), "sku");
    ValidationUtils.validateNotBlank(productDTO.getName(), "name");
    ValidationUtils.validateNonNegative(productDTO.getPrice(), "price");
    ValidationUtils.validateNonNegative(BigDecimal.valueOf(productDTO.getStockQty()), "stockQty");

    if (existsBySku(productDTO.getSku())) {
        throw new DuplicateEntityException("Product", "sku", productDTO.getSku());
    }

    Category category = categoryService.findCategoryEntityOrThrow(productDTO.getCategoryId());

    Product product = productMapper.toEntity(productDTO);
    product.setCategory(category);
    product.setCreatedAt(LocalDateTime.now());
    category.getProducts().add(product);

    product = productRepository.save(product);

    return productMapper.toDTO(product);
}
```

## Reemplaza `updateProduct(...)`

```java
@Transactional
public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
    Product product = findProductEntityOrThrow(productId);

    if (!product.getSku().equals(productDTO.getSku()) && existsBySku(productDTO.getSku())) {
        throw new DuplicateEntityException("Product", "sku", productDTO.getSku());
    }

    ValidationUtils.validateNotBlank(productDTO.getName(), "name");
    ValidationUtils.validateNonNegative(productDTO.getPrice(), "price");
    ValidationUtils.validateNonNegative(BigDecimal.valueOf(productDTO.getStockQty()), "stockQty");

    product.setSku(productDTO.getSku());
    product.setName(productDTO.getName());
    product.setDescription(productDTO.getDescription());
    product.setPrice(productDTO.getPrice());
    product.setStockQty(productDTO.getStockQty());
    product.setIsActive(productDTO.getIsActive());

    if (productDTO.getCategoryId() != null &&
        !productDTO.getCategoryId().equals(product.getCategory().getCategoryId())) {
        Category currentCategory = product.getCategory();
        Category newCategory = categoryService.findCategoryEntityOrThrow(productDTO.getCategoryId());
        currentCategory.getProducts().remove(product);
        newCategory.getProducts().add(product);
        product.setCategory(newCategory);
    }

    product = productRepository.save(product);

    return productMapper.toDTO(product);
}
```

## Reemplaza búsquedas y persistencia

```java
public ProductDTO findBySku(String sku) {
    Product product = productRepository.findBySkuIgnoreCase(sku)
            .orElseThrow(() -> new EntityNotFoundException("Product with SKU: " + sku));

    return productMapper.toDTO(product);
}

public List<ProductDTO> findAllProducts() {
    return productMapper.toDTOList(productRepository.findAll());
}

public List<ProductDTO> findActiveProducts() {
    return productMapper.toDTOList(productRepository.findByIsActiveTrue());
}

public List<ProductDTO> findByCategory(Long categoryId) {
    categoryService.findCategoryEntityOrThrow(categoryId);
    return productMapper.toDTOList(productRepository.findByCategory_CategoryId(categoryId));
}

public List<ProductDTO> searchByName(String name) {
    return productMapper.toDTOList(productRepository.findByNameContainingIgnoreCase(name));
}

public boolean existsBySku(String sku) {
    return productRepository.existsBySkuIgnoreCase(sku);
}

public Product findProductEntityOrThrow(Long productId) {
    return productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product", productId));
}
```

---

# 10. Refactorizar `CartService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/CartService.java`

## Reemplaza cabecera, atributos y constructor

```java
@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartMapper cartMapper;
    private final UserService userService;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final UserSessionRepository userSessionRepository;

    public CartService(UserService userService, ProductService productService,
                       CartRepository cartRepository, UserSessionRepository userSessionRepository) {
        this.cartMapper = new CartMapper();
        this.userService = userService;
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.userSessionRepository = userSessionRepository;
    }
```

## Reemplaza `createCartForGuest(...)`

```java
@Transactional
public CartDTO createCartForGuest(Long sessionId) {
    Cart cart = Cart.builder()
            .user(null)
            .session(resolveSession(sessionId))
            .status(CartStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    cart = cartRepository.save(cart);

    return cartMapper.toDTO(cart);
}
```

## Reemplaza `createCartForUser(...)`

```java
@Transactional
public CartDTO createCartForUser(Long userId) {
    User user = userService.findUserEntityOrThrow(userId);

    Cart cart = Cart.builder()
            .user(user)
            .status(CartStatus.OPEN)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    cart = cartRepository.save(cart);

    return cartMapper.toDTO(cart);
}
```

## Reemplaza `findOpenCartByUser(...)`

```java
public CartDTO findOpenCartByUser(Long userId) {
    Cart cart = cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN)
            .orElse(null);

    return cart != null ? cartMapper.toDTO(cart) : null;
}
```

## Reemplaza `addItem(...)`

```java
@Transactional
public CartDTO addItem(Long cartId, Long productId, Integer quantity) {
    ValidationUtils.validatePositive(quantity, "quantity");

    Cart cart = findCartEntityOrThrow(cartId);
    if (cart.getStatus() != CartStatus.OPEN) {
        throw new InvalidCartStateException(cartId, cart.getStatus(),
            CartStatus.OPEN, "add item");
    }

    Product product = productService.findProductEntityOrThrow(productId);
    if (!Boolean.TRUE.equals(product.getIsActive())) {
        throw new ValidationException("Product '" + product.getName() + "' is not active");
    }

    if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
        throw new InsufficientStockException(productId, product.getSku(),
            quantity, product.getStockQty());
    }

    CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getProductId().equals(productId))
            .findFirst()
            .orElse(null);

    if (existingItem != null) {
        int newQuantity = existingItem.getQuantity() + quantity;

        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
            throw new InsufficientStockException(productId, product.getSku(),
                newQuantity, product.getStockQty());
        }

        existingItem.setQuantity(newQuantity);
    } else {
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .addedAt(LocalDateTime.now())
                .build();

        cart.getItems().add(newItem);
        newItem.setCart(cart);
    }

    touchCart(cart);
    cart = cartRepository.save(cart);

    return cartMapper.toDTO(cart);
}
```

## Reemplaza `mergeGuestCartToUserCart(...)`

```java
@Transactional
public CartDTO mergeGuestCartToUserCart(Long guestCartId, Long userId) {
    Cart guestCart = findCartEntityOrThrow(guestCartId);
    Cart userCart = findOrCreateOpenCartForUser(userId);

    if (guestCart.getStatus() != CartStatus.OPEN) {
        throw new InvalidCartStateException(guestCartId, guestCart.getStatus(),
            CartStatus.OPEN, "merge");
    }
    if (userCart.getStatus() != CartStatus.OPEN) {
        throw new InvalidCartStateException(userCart.getCartId(),
            userCart.getStatus(), CartStatus.OPEN, "merge");
    }

    if (guestCart.getUser() != null) {
        throw new CartMergeException(guestCartId, userCart.getCartId(),
            "Guest cart already has a user assigned");
    }

    for (CartItem guestItem : new ArrayList<>(guestCart.getItems())) {
        Product product = guestItem.getProduct();
        Integer guestQuantity = guestItem.getQuantity();

        CartItem userItem = userCart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                .findFirst()
                .orElse(null);

        if (userItem != null) {
            int totalQuantity = userItem.getQuantity() + guestQuantity;

            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), totalQuantity)) {
                throw new InsufficientStockException(product.getProductId(),
                    product.getSku(), totalQuantity, product.getStockQty());
            }

            userItem.setQuantity(totalQuantity);

            if (guestItem.getAddedAt().isAfter(userItem.getAddedAt())) {
                userItem.setUnitPrice(guestItem.getUnitPrice());
            }
        } else {
            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), guestQuantity)) {
                throw new InsufficientStockException(product.getProductId(),
                    product.getSku(), guestQuantity, product.getStockQty());
            }

            CartItem newItem = CartItem.builder()
                    .cart(userCart)
                    .product(product)
                    .quantity(guestQuantity)
                    .unitPrice(guestItem.getUnitPrice())
                    .addedAt(guestItem.getAddedAt())
                    .build();

            userCart.getItems().add(newItem);
        }
    }

    guestCart.setStatus(CartStatus.ABANDONED);
    touchCart(guestCart);

    touchCart(userCart);

    cartRepository.save(guestCart);
    userCart = cartRepository.save(userCart);

    return cartMapper.toDTO(userCart);
}
```

## Reemplaza utilitarios base

```java
public Cart findCartEntityOrThrow(Long cartId) {
    return cartRepository.findById(cartId)
            .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
}

private Cart findOrCreateOpenCartForUser(Long userId) {
    User user = userService.findUserEntityOrThrow(userId);

    return cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN)
            .orElseGet(() -> cartRepository.save(Cart.builder()
                    .user(user)
                    .status(CartStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()));
}

private UserSession resolveSession(Long sessionId) {
    if (sessionId == null) {
        return null;
    }

    return userSessionRepository.findById(sessionId)
            .orElseThrow(() -> new EntityNotFoundException("UserSession", sessionId));
}
```

---

# 11. Refactorizar `OrderService`

## Archivo
`src/main/java/co/edu/cesde/pps/service/OrderService.java`

## Reemplaza cabecera, atributos y constructor

```java
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final Random random;

    public OrderService(UserService userService, CartService cartService,
                       AddressService addressService, ProductService productService,
                       OrderRepository orderRepository, OrderStatusRepository orderStatusRepository) {
        this.orderMapper = new OrderMapper();
        this.userService = userService;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.random = new Random();
    }
```

## Reemplaza `checkout(...)`

```java
@Transactional
public OrderDTO checkout(Long userId, Long cartId, Long shippingAddressId,
                        Long billingAddressId) {
    User user = userService.findUserEntityOrThrow(userId);
    Cart cart = cartService.findCartEntityOrThrow(cartId);

    if (cart.getStatus() != CartStatus.OPEN) {
        throw new InvalidCartStateException(cartId, cart.getStatus(),
            CartStatus.OPEN, "checkout");
    }

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
        throw new ValidationException("Cannot checkout empty cart");
    }

    if (cart.getUser() == null || !cart.getUser().getUserId().equals(userId)) {
        throw new ValidationException("Cart does not belong to user");
    }

    Address shippingAddress = addressService.findAddressEntityOrThrow(shippingAddressId);
    Address billingAddress = addressService.findAddressEntityOrThrow(billingAddressId);

    if (!shippingAddress.getUser().getUserId().equals(userId)) {
        throw new ValidationException("Shipping address does not belong to user");
    }
    if (!billingAddress.getUser().getUserId().equals(userId)) {
        throw new ValidationException("Billing address does not belong to user");
    }

    for (CartItem item : cart.getItems()) {
        Product product = item.getProduct();

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ValidationException("Product '" + product.getName() +
                "' is no longer available");
        }

        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), item.getQuantity())) {
            throw new InsufficientStockException(product.getProductId(),
                product.getSku(), item.getQuantity(), product.getStockQty());
        }
    }

    String orderNumber = generateOrderNumber();

    OrderStatus pendingStatus = orderStatusRepository.findByNameIgnoreCase("PENDING")
            .orElseThrow(() -> new EntityNotFoundException("OrderStatus", "PENDING"));

    Order order = Order.builder()
            .orderNumber(orderNumber)
            .user(user)
            .orderStatus(pendingStatus)
            .shippingAddress(shippingAddress)
            .billingAddress(billingAddress)
            .subtotal(BigDecimal.ZERO)
            .tax(BigDecimal.ZERO)
            .shippingCost(BigDecimal.ZERO)
            .total(BigDecimal.ZERO)
            .createdAt(LocalDateTime.now())
            .build();

    for (CartItem cartItem : cart.getItems()) {
        BigDecimal lineTotal = CalculationUtils.calculateOrderItemLineTotal(
                cartItem.getUnitPrice(), cartItem.getQuantity());

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .lineTotal(lineTotal)
                .build();

        order.getItems().add(orderItem);
        orderItem.setOrder(order);
    }

    List<BigDecimal> lineTotals = order.getItems().stream()
            .map(OrderItem::getLineTotal)
            .toList();

    BigDecimal subtotal = CalculationUtils.calculateOrderSubtotal(lineTotals);
    BigDecimal taxRate = BigDecimal.valueOf(AppConfig.getDefaultTaxRate());
    BigDecimal tax = CalculationUtils.calculateTax(subtotal, taxRate);
    BigDecimal shippingCost = calculateShippingCost(subtotal);
    BigDecimal total = CalculationUtils.calculateOrderTotal(subtotal, tax, shippingCost);

    order.setSubtotal(subtotal);
    order.setTax(tax);
    order.setShippingCost(shippingCost);
    order.setTotal(total);

    for (CartItem item : cart.getItems()) {
        productService.decreaseStock(item.getProduct().getProductId(),
            item.getQuantity());
    }

    cart.setStatus(CartStatus.CONVERTED);
    cart.setUpdatedAt(LocalDateTime.now());

    order = orderRepository.save(order);

    return orderMapper.toDTO(order);
}
```

## Reemplaza consultas y generación de número

```java
public OrderDTO findByOrderNumber(String orderNumber) {
    Order order = orderRepository.findByOrderNumberIgnoreCase(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException("Order with number: " + orderNumber));

    return orderMapper.toDTO(order);
}

public List<OrderDTO> findOrdersByUser(Long userId) {
    userService.findUserEntityOrThrow(userId);
    return orderMapper.toDTOList(orderRepository.findByUser_UserId(userId));
}

public List<OrderDTO> findOrdersByStatus(Long statusId) {
    return orderMapper.toDTOList(orderRepository.findByOrderStatus_OrderStatusId(statusId));
}

public List<OrderDTO> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return orderMapper.toDTOList(orderRepository.findByCreatedAtBetween(startDate, endDate));
}

public String generateOrderNumber() {
    String prefix = AppConfig.getOrderNumberPrefix();
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    String orderNumber;
    do {
        String randomPart = String.format("%06d", random.nextInt(1000000));
        orderNumber = prefix + date + "-" + randomPart;
    } while (orderRepository.existsByOrderNumberIgnoreCase(orderNumber));

    return orderNumber;
}

public Order findOrderEntityOrThrow(Long orderId) {
    return orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
}
```

## Punto crítico de esta etapa
En `checkout(...)` ya **no** se asignan IDs sueltos.
Se asignan **entidades reales**:

```java
.user(user)
.shippingAddress(shippingAddress)
.billingAddress(billingAddress)
.orderStatus(pendingStatus)
```

---

# 12. Crear perfil de pruebas

## Archivo nuevo
`src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:pps-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    open-in-view: false
    show-sql: false
    properties:
      hibernate:
        format_sql: true
  sql:
    init:
      mode: never

logging:
  level:
    root: WARN
    co.edu.cesde.pps: INFO
    org.hibernate.SQL: WARN
    org.hibernate.orm.jdbc.bind: WARN
```

---

# 13. Crear prueba de integración

## Archivo nuevo
`src/test/java/co/edu/cesde/pps/Etapa10SpringBootIntegrationTest.java`

```java
package co.edu.cesde.pps;

import co.edu.cesde.pps.dto.AddressDTO;
import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.dto.OrderDTO;
import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.dto.UserDTO;
import co.edu.cesde.pps.enums.AddressType;
import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.model.OrderStatus;
import co.edu.cesde.pps.model.Role;
import co.edu.cesde.pps.model.Cart;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.repository.AddressRepository;
import co.edu.cesde.pps.repository.CartRepository;
import co.edu.cesde.pps.repository.CategoryRepository;
import co.edu.cesde.pps.repository.OrderRepository;
import co.edu.cesde.pps.repository.OrderStatusRepository;
import co.edu.cesde.pps.repository.ProductRepository;
import co.edu.cesde.pps.repository.RoleRepository;
import co.edu.cesde.pps.repository.UserRepository;
import co.edu.cesde.pps.service.AddressService;
import co.edu.cesde.pps.service.CartService;
import co.edu.cesde.pps.service.CategoryService;
import co.edu.cesde.pps.service.OrderService;
import co.edu.cesde.pps.service.ProductService;
import co.edu.cesde.pps.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Etapa10SpringBootIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        orderStatusRepository.deleteAll();
        roleRepository.deleteAll();

        roleRepository.save(Role.builder()
                .name("CUSTOMER")
                .description("Regular customer user")
                .build());

        orderStatusRepository.save(OrderStatus.builder()
                .name("PENDING")
                .description("Order created, awaiting payment")
                .build());
    }

    @Test
    void shouldRegisterUserCreateCartAndCheckoutWithJpaRepositories() {
        CategoryDTO category = new CategoryDTO();
        category.setName("Laptops");
        CategoryDTO persistedCategory = categoryService.createCategory(category);

        ProductDTO product = new ProductDTO();
        product.setCategoryId(persistedCategory.getCategoryId());
        product.setSku("LAP-001");
        product.setName("Ultrabook Pro");
        product.setDescription("Notebook de prueba para checkout");
        product.setPrice(new BigDecimal("3500.00"));
        product.setStockQty(8);
        product.setIsActive(true);
        ProductDTO persistedProduct = productService.createProduct(product);

        UserDTO user = userService.registerUser(
                "student@cesde.edu.co",
                "hashed-password",
                "Ada",
                "Lovelace",
                "3001234567"
        );

        AddressDTO shipping = buildAddress(AddressType.SHIPPING, true, "Calle 10 # 20-30");
        AddressDTO billing = buildAddress(AddressType.BILLING, false, "Carrera 15 # 40-50");
        AddressDTO persistedShipping = addressService.addAddress(user.getUserId(), shipping);
        AddressDTO persistedBilling = addressService.addAddress(user.getUserId(), billing);

        CartDTO cart = cartService.createCartForUser(user.getUserId());
        CartDTO cartWithItem = cartService.addItem(cart.getCartId(), persistedProduct.getProductId(), 2);

        OrderDTO order = orderService.checkout(
                user.getUserId(),
                cart.getCartId(),
                persistedShipping.getAddressId(),
                persistedBilling.getAddressId()
        );

        assertThat(cartWithItem.getItems()).hasSize(1);
        assertThat(order.getOrderId()).isNotNull();
        assertThat(order.getOrderNumber()).startsWith("ORD-");
        assertThat(order.getUserId()).isEqualTo(user.getUserId());
        assertThat(order.getOrderStatusName()).isEqualTo("PENDING");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getSubtotal()).isEqualByComparingTo("7000.00");
        assertThat(order.getTax()).isEqualByComparingTo("1330.00");
        assertThat(order.getShippingCost()).isEqualByComparingTo("0.00");
        assertThat(order.getTotal()).isEqualByComparingTo("8330.00");
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(cartRepository.findById(cart.getCartId())).get()
                .extracting(Cart::getStatus)
                .isEqualTo(CartStatus.CONVERTED);
        assertThat(productRepository.findById(persistedProduct.getProductId())).get()
                .extracting(Product::getStockQty)
                .isEqualTo(6);
    }

    private AddressDTO buildAddress(AddressType type, boolean isDefault, String line1) {
        AddressDTO dto = new AddressDTO();
        dto.setType(type);
        dto.setLine1(line1);
        dto.setCity("Medellín");
        dto.setState("Antioquia");
        dto.setCountry("Colombia");
        dto.setPostalCode("050001");
        dto.setIsDefault(isDefault);
        return dto;
    }
}
```

---

# 14. Validación final

Ejecuta:

```bash
mvn -q -DskipTests compile
mvn -q test
```

---

# 15. Checklist de revisión para el estudiante

## Infraestructura
- [ ] `pom.xml` quedó con Spring Boot parent
- [ ] existe `PpsApplication.java`
- [ ] existe `application.yml`
- [ ] existe `application-test.yml`

## Repositorios
- [ ] se creó el package `repository`
- [ ] existen los 9 repositorios de `etapa10`

## Models
- [ ] `Cart.items` tiene `cascade = CascadeType.ALL`
- [ ] `Cart.items` tiene `orphanRemoval = true`
- [ ] `Order.items` tiene `cascade = CascadeType.ALL`
- [ ] `Order.items` tiene `orphanRemoval = true`

## Services
- [ ] ya no existen listas `inMemory`
- [ ] ya no existen `generateNextId()`
- [ ] `UserService` carga `CUSTOMER` desde `RoleRepository`
- [ ] `OrderService` carga `PENDING` desde `OrderStatusRepository`
- [ ] `checkout(...)` construye `Order` con entidades reales

## Validación funcional
- [ ] el proyecto compila
- [ ] el test de integración pasa
- [ ] el carrito cambia a `CONVERTED`
- [ ] el stock disminuye después del checkout

---

# 16. Qué sigue en ETAPA 11
La siguiente etapa ya sí puede enfocarse en:

- `@RestController`
- endpoints REST
- `@RestControllerAdvice`
- respuestas HTTP
- DTOs de request/response
- manejo de lazy loading y serialización

Porque en `etapa10` ya quedó lista la persistencia real.

