use db_java_spring_boot_ecommerce_bd02;
select * from category;
select * from product;
select * from roles;
select * from users;
select * from user_roles;
select * from refresh_tokens;
select * from revoked_tokens;

-- --- PASO 2: Generar e insertar 1000 productos aleatorios para MySQL ---
DELIMITER //

CREATE PROCEDURE InsertRandomProducts()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 1000 DO
        INSERT INTO product (name, description, price, stock_quantity, image_url, category_id, created_at)
        VALUES (
            -- Nombre del producto
            CONCAT(
                'Producto Electrónico ', LPAD(FLOOR(RAND() * 100000), 5, '0'), ' - ',
                CASE FLOOR(RAND() * 5)
                    WHEN 0 THEN 'Smartphone'
                    WHEN 1 THEN 'Laptop'
                    WHEN 2 THEN 'Smart TV'
                    WHEN 3 THEN 'Auriculares'
                    ELSE 'Smartwatch'
                END, ' ',
                CASE FLOOR(RAND() * 4)
                    WHEN 0 THEN 'Pro'
                    WHEN 1 THEN 'Ultra'
                    WHEN 2 THEN 'Max'
                    ELSE 'Mini'
                END
            ),

            -- Descripción del producto
            CONCAT(
                'Este es un dispositivo electrónico de alta calidad, ideal para ',
                CASE FLOOR(RAND() * 3)
                    WHEN 0 THEN 'trabajo y ocio.'
                    WHEN 1 THEN 'gaming y productividad.'
                    ELSE 'uso diario con características innovadoras.'
                END, ' Ofrece una experiencia única con su ',
                CASE FLOOR(RAND() * 4)
                    WHEN 0 THEN 'pantalla OLED vibrante'
                    WHEN 1 THEN 'procesador de última generación'
                    WHEN 2 THEN 'batería de larga duración'
                    ELSE 'cámara de alta resolución'
                END, '.'
            ),

            -- Precio (entre 50.00 y 1500.00)
            ROUND(50.00 + (RAND() * (1500.00 - 50.00)), 2),

            -- Stock (entre 10 y 500)
            FLOOR(RAND() * (500 - 10 + 1) + 10),

            -- URL de la imagen (simulada)
            CONCAT('https://picsum.photos/seed/', FLOOR(RAND() * 10000), '/400/300'),

            -- category_id (siempre 1 para 'Electrónica')
            1,

            -- created_at (fecha y hora actual)
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

-- Llama al procedimiento para insertar los productos
CALL db_java_spring_boot_ecommerce_bd02.InsertRandomProducts();

-- Opcional: Elimina el procedimiento después de usarlo
DROP PROCEDURE InsertRandomProducts;