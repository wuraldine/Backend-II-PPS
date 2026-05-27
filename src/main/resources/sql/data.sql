-- ============================================
-- Product Purchasing System - Initial Data
-- ============================================
--
-- Este script inserta datos iniciales para testing y desarrollo.
--
-- Para ejecutar:
-- mysql -u root -p pps_db < data.sql
-- ============================================

-- ============================================
-- Datos: roles
-- ============================================
INSERT INTO roles (role_id, name, description) VALUES
(1, 'ADMIN', 'Administrator with full access'),
(2, 'CUSTOMER', 'Regular customer user'),
(3, 'MANAGER', 'Store manager with elevated privileges')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: order_statuses
-- ============================================
INSERT INTO order_statuses (order_status_id, name, description) VALUES
(1, 'PENDING', 'Order created, awaiting payment'),
(2, 'CONFIRMED', 'Payment confirmed, processing order'),
(3, 'PROCESSING', 'Order is being prepared'),
(4, 'SHIPPED', 'Order has been shipped'),
(5, 'DELIVERED', 'Order delivered successfully'),
(6, 'CANCELLED', 'Order was cancelled'),
(7, 'REFUNDED', 'Order was refunded')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: payment_methods
-- ============================================
INSERT INTO payment_methods (payment_method_id, name, description) VALUES
(1, 'CREDIT_CARD', 'Credit Card Payment'),
(2, 'DEBIT_CARD', 'Debit Card Payment'),
(3, 'PAYPAL', 'PayPal Payment'),
(4, 'BANK_TRANSFER', 'Bank Transfer'),
(5, 'CASH_ON_DELIVERY', 'Cash on Delivery')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: payment_statuses
-- ============================================
INSERT INTO payment_statuses (payment_status_id, name, description) VALUES
(1, 'PENDING', 'Payment pending'),
(2, 'PROCESSING', 'Payment being processed'),
(3, 'COMPLETED', 'Payment completed successfully'),
(4, 'FAILED', 'Payment failed'),
(5, 'REFUNDED', 'Payment refunded'),
(6, 'CANCELLED', 'Payment cancelled')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- ============================================
-- Datos: categories (Jerarquía de ejemplo)
-- ============================================
-- Categorías raíz
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(1, NULL, 'Electronics', 'electronics'),
(2, NULL, 'Clothing', 'clothing'),
(3, NULL, 'Books', 'books'),
(4, NULL, 'Home & Garden', 'home-garden')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Electronics
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(11, 1, 'Computers', 'computers'),
(12, 1, 'Smartphones', 'smartphones'),
(13, 1, 'Audio', 'audio'),
(14, 1, 'Cameras', 'cameras')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Clothing
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(21, 2, 'Men', 'men-clothing'),
(22, 2, 'Women', 'women-clothing'),
(23, 2, 'Kids', 'kids-clothing')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Subcategorías de Books
INSERT INTO categories (category_id, parent_id, name, slug) VALUES
(31, 3, 'Fiction', 'fiction'),
(32, 3, 'Non-Fiction', 'non-fiction'),
(33, 3, 'Technical', 'technical-books')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================
-- Datos: products (Ejemplos)
-- ============================================
-- Productos de Electronics > Computers
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active, image) VALUES
(1, 11, 'COMP-LAP-001', 'Dell XPS 13 Laptop', 'Ultrabook de alto rendimiento con procesador Intel Core i7 de 13a generación, pantalla OLED de 13.4 pulgadas Full HD+, 16 GB de RAM LPDDR5 y SSD NVMe de 512 GB. Diseño compacto y liviano ideal para profesionales y estudiantes que requieren potencia y portabilidad.', 5200000.00, 15, TRUE, 'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&fit=crop&auto=format'),
(2, 11, 'COMP-LAP-002', 'MacBook Air M2', 'Laptop ultradelgada de Apple con chip M2 de última generación, pantalla Liquid Retina de 13.6 pulgadas, 8 GB de memoria unificada y SSD de 256 GB. Batería de hasta 18 horas de autonomía, diseño sin ventilador y rendimiento excepcional para creativos y profesionales.', 4800000.00, 10, TRUE, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&fit=crop&auto=format'),
(3, 11, 'COMP-DES-001', 'Gaming Desktop PC', 'PC de escritorio para gaming de alto nivel equipado con procesador Intel Core i9, tarjeta gráfica NVIDIA GeForce RTX 4080 de 16 GB, 32 GB de RAM DDR5 y SSD NVMe de 1 TB. Torre con iluminación RGB, refrigeración líquida y chasis optimizado para máximo airflow.', 9900000.00, 5, TRUE, 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&fit=crop&auto=format')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), stock_qty=VALUES(stock_qty), image=VALUES(image);

-- Productos de Electronics > Smartphones
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active, image) VALUES
(4, 12, 'PHONE-IP-001', 'iPhone 15 Pro', 'Smartphone insignia de Apple con chip A17 Pro de 3 nm, pantalla Super Retina XDR de 6.1 pulgadas con ProMotion a 120 Hz, sistema de cámara Pro de 48 MP con zoom óptico 3x y conector USB-C con velocidades USB 3. Cuerpo en titanio grado aeroespacial, resistente al agua IP68.', 4200000.00, 20, TRUE, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&fit=crop&auto=format'),
(5, 12, 'PHONE-SAM-001', 'Samsung Galaxy S24', 'Teléfono insignia de Samsung con procesador Snapdragon 8 Gen 3, pantalla Dynamic AMOLED 2X de 6.2 pulgadas a 120 Hz, cámara principal de 50 MP con inteligencia artificial avanzada y batería de 4000 mAh con carga rápida de 25 W. Incluye funciones Galaxy AI para productividad y creatividad.', 3500000.00, 25, TRUE, 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600&fit=crop&auto=format'),
(6, 12, 'PHONE-PIX-001', 'Google Pixel 8', 'Smartphone de Google con chip Tensor G3 diseñado por Google, cámara de 50 MP con capacidades de inteligencia artificial como Borrador Mágico y Foto sin desenfoque, pantalla OLED de 6.2 pulgadas a 120 Hz y 7 años garantizados de actualizaciones de Android y seguridad.', 2800000.00, 15, TRUE, 'https://images.unsplash.com/photo-1598327105854-c8674faddf79?w=600&fit=crop&auto=format')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), stock_qty=VALUES(stock_qty), image=VALUES(image);

-- Productos de Electronics > Audio
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active, image) VALUES
(7, 13, 'AUD-HEAD-001', 'Sony WH-1000XM5', 'Audífonos inalámbricos over-ear con cancelación de ruido líder en la industria, respaldada por dos procesadores y ocho micrófonos. Audio de alta resolución, hasta 30 horas de batería, conexión multipunto a dos dispositivos simultáneos y diseño ultraliviano plegable para mayor comodidad en viajes.', 1600000.00, 30, TRUE, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&fit=crop&auto=format'),
(8, 13, 'AUD-SPEAK-001', 'JBL Flip 6', 'Parlante Bluetooth portátil con sonido potente de 20 W, bajos profundos y dos tweeters. Resistente al agua y al polvo con certificación IP67, batería de 12 horas de reproducción, modo Partyboost para conectar múltiples parlantes JBL y diseño compacto ideal para exteriores.', 520000.00, 50, TRUE, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&fit=crop&auto=format')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), stock_qty=VALUES(stock_qty), image=VALUES(image);

-- Productos de Clothing > Men
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active, image) VALUES
(9, 21, 'CLOTH-MEN-001', 'Classic Denim Jeans', 'Jean de corte recto en denim 100% algodón de alta calidad, con lavado clásico azul medio. Cintura ajustable, bolsillos funcionales y costuras reforzadas para mayor durabilidad. Tallas disponibles del 28 al 42. Ideal para un look casual o semiformal.', 240000.00, 100, TRUE, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&fit=crop&auto=format'),
(10, 21, 'CLOTH-MEN-002', 'Cotton T-Shirt', 'Camiseta básica de algodón peinado 180 g/m², suave al tacto y de larga duración. Cuello redondo reforzado, costuras dobles en mangas y bajo, y corte regular que favorece todo tipo de silueta. Disponible en tallas XS a XXL y múltiples colores.', 80000.00, 200, TRUE, 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&fit=crop&auto=format')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), stock_qty=VALUES(stock_qty), image=VALUES(image);

-- Productos de Books > Technical
INSERT INTO products (product_id, category_id, sku, name, description, price, stock_qty, is_active, image) VALUES
(11, 33, 'BOOK-TECH-001', 'Clean Code', 'Libro de Robert C. Martin que presenta principios, patrones y prácticas para escribir código limpio, legible y mantenible. Cubre nombres significativos, funciones cortas, manejo de errores, pruebas unitarias y refactorización. Lectura obligatoria para cualquier desarrollador de software profesional.', 180000.00, 50, TRUE, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600&fit=crop&auto=format'),
(12, 33, 'BOOK-TECH-002', 'Design Patterns', 'Obra clásica de la "Gang of Four" (Gamma, Helm, Johnson, Vlissides) que cataloga 23 patrones de diseño orientado a objetos reutilizables. Incluye patrones creacionales, estructurales y de comportamiento con ejemplos prácticos. Base fundamental del desarrollo de software moderno.', 220000.00, 40, TRUE, 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=600&fit=crop&auto=format'),
(13, 33, 'BOOK-TECH-003', 'Java Persistence with Hibernate', 'Guía completa sobre JPA e Hibernate para el mapeo objeto-relacional en Java. Cubre configuración, mapeo de entidades, consultas JPQL y Criteria API, gestión de transacciones, caché y estrategias de rendimiento. Incluye ejemplos con Spring Boot y bases de datos relacionales.', 190000.00, 30, TRUE, 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=600&fit=crop&auto=format')
ON DUPLICATE KEY UPDATE description=VALUES(description), price=VALUES(price), stock_qty=VALUES(stock_qty), image=VALUES(image);

-- ============================================
-- Datos: users (Ejemplos para testing)
-- ============================================
-- NOTA: Los password_hash aquí son placeholders temporales.
-- DataInitializerConfig los reemplaza con hashes BCrypt reales al arrancar.
-- Contraseñas de desarrollo:
--   admin@pps.com      -> admin123
--   john.doe@email.com -> customer123
--   jane.smith@email.com -> customer123
--   manager@pps.com    -> manager123
INSERT INTO users (user_id, role_id, email, password_hash, first_name, last_name, phone, status) VALUES
(1, 1, 'admin@pps.com', 'PLACEHOLDER', 'Admin', 'User', '555-0001', 'ACTIVE'),
(2, 2, 'john.doe@email.com', 'PLACEHOLDER', 'John', 'Doe', '555-0100', 'ACTIVE'),
(3, 2, 'jane.smith@email.com', 'PLACEHOLDER', 'Jane', 'Smith', '555-0101', 'ACTIVE'),
(4, 3, 'manager@pps.com', 'PLACEHOLDER', 'Store', 'Manager', '555-0002', 'ACTIVE')
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- ============================================
-- Datos: addresses (Ejemplos)
-- ============================================
INSERT INTO addresses (user_id, type, line1, line2, city, state, country, postal_code, is_default) VALUES
(2, 'SHIPPING', '123 Main Street', 'Apt 4B', 'New York', 'NY', 'USA', '10001', TRUE),
(2, 'BILLING', '123 Main Street', 'Apt 4B', 'New York', 'NY', 'USA', '10001', TRUE),
(3, 'SHIPPING', '456 Oak Avenue', NULL, 'Los Angeles', 'CA', 'USA', '90001', TRUE),
(3, 'BILLING', '456 Oak Avenue', NULL, 'Los Angeles', 'CA', 'USA', '90001', TRUE)
ON DUPLICATE KEY UPDATE line1=VALUES(line1);

-- ============================================
-- Verificación de datos insertados
-- ============================================
-- SELECT * FROM roles;
-- SELECT * FROM categories WHERE parent_id IS NULL;
-- SELECT * FROM products LIMIT 5;
-- SELECT * FROM users;
