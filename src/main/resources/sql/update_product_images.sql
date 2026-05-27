-- ============================================
-- Actualización de imágenes de productos
-- Ejecutar en: pps_db
-- ============================================

UPDATE products SET image = 'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&fit=crop&auto=format' WHERE product_id = 1;
UPDATE products SET image = 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&fit=crop&auto=format' WHERE product_id = 2;
UPDATE products SET image = 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&fit=crop&auto=format' WHERE product_id = 3;
UPDATE products SET image = 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=600&fit=crop&auto=format' WHERE product_id = 4;
UPDATE products SET image = 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600&fit=crop&auto=format' WHERE product_id = 5;
UPDATE products SET image = 'https://images.unsplash.com/photo-1598327105854-c8674faddf79?w=600&fit=crop&auto=format' WHERE product_id = 6;
UPDATE products SET image = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&fit=crop&auto=format' WHERE product_id = 7;
UPDATE products SET image = 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&fit=crop&auto=format' WHERE product_id = 8;
UPDATE products SET image = 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&fit=crop&auto=format' WHERE product_id = 9;
UPDATE products SET image = 'https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&fit=crop&auto=format' WHERE product_id = 10;
UPDATE products SET image = 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=600&fit=crop&auto=format' WHERE product_id = 11;
UPDATE products SET image = 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=600&fit=crop&auto=format' WHERE product_id = 12;
UPDATE products SET image = 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=600&fit=crop&auto=format' WHERE product_id = 13;

-- Verificación
SELECT product_id, name, image FROM products ORDER BY product_id;