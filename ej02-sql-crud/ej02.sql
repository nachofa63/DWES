-- 1. Inserta el producto Auriculares a 39.95 y consulta su ID asignado
INSERT INTO producto (nombre, precio) VALUES ('Auriculares', 39.95);
SELECT LAST_INSERT_ID(); -- Debería devolver 4 (en un servidor limpio)

-- 2. Nombre y precio de productos de más de 20 €, del más caro al más barato
SELECT nombre, precio FROM producto WHERE precio > 20 ORDER BY precio DESC;

-- 3. ¿Cuántos pedidos ha hecho el cliente 1?
SELECT COUNT(*) FROM pedido WHERE cliente_id = 1;

-- 4. ID y fecha de los pedidos del cliente con email ana@ejemplo.es (usando JOIN)
SELECT p.id, p.fecha 
FROM pedido p 
JOIN cliente c ON p.cliente_id = c.id 
WHERE c.email = 'ana@ejemplo.es';

-- 5. Nombre del producto y cantidad de las líneas del pedido 1
SELECT pr.nombre, lp.cantidad 
FROM linea_pedido lp 
JOIN producto pr ON lp.producto_id = pr.id 
WHERE lp.pedido_id = 1;

-- 6. Total de cada pedido (suma de cantidad * precio actual), ordenado por pedido
SELECT lp.pedido_id, SUM(lp.cantidad * pr.precio) AS total_pedido 
FROM linea_pedido lp 
JOIN producto pr ON lp.producto_id = pr.id 
GROUP BY lp.pedido_id 
ORDER BY lp.pedido_id;

-- 7. Sube un 10% el precio de los productos de menos de 20 € y consulta el Ratón
UPDATE producto SET precio = precio * 1.10 WHERE precio < 20;
SELECT * FROM producto WHERE nombre = 'Ratón';

-- 8. Borra el producto Auriculares
DELETE FROM producto WHERE nombre = 'Auriculares';

-- 9. Intenta borrar el producto con id = 1 (Teclado)
DELETE FROM producto WHERE id = 1; 
-- Da Error 1451 porque está siendo referenciado en una línea de pedido (Foreign Key).

-- 10. Transacción de prueba: poner precios a 0, consultar, y deshacer con ROLLBACK
START TRANSACTION;
UPDATE producto SET precio = 0;
SELECT * FROM producto; -- Todo a 0.00
ROLLBACK;
SELECT * FROM producto; -- Vuelven a su precio anterior