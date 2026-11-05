-- ej02 · Esquema ampliado de la tienda (clientes, pedidos y líneas de pedido).
-- Ejecútalo en DBeaver (Alt+X) sobre la base de datos `tienda`.
-- Es repetible: borra y vuelve a crear estas tres tablas (NO toca `producto`).
-- Necesita la tabla `producto` que ya crea el contenedor (recursos/mysql-docker/init).
SET NAMES utf8mb4;

DROP TABLE IF EXISTS linea_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS producto;

CREATE TABLE producto (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  precio DECIMAL(8,2) NOT NULL CHECK (precio >= 0)
) CHARACTER SET utf8mb4;

CREATE TABLE cliente (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  email  VARCHAR(150) NOT NULL UNIQUE
) CHARACTER SET utf8mb4;

CREATE TABLE pedido (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id INT      NOT NULL,
  fecha      DATETIME NOT NULL,
  FOREIGN KEY (cliente_id) REFERENCES cliente (id)
) CHARACTER SET utf8mb4;

CREATE TABLE linea_pedido (
  pedido_id   INT NOT NULL,
  producto_id INT NOT NULL,
  cantidad    INT NOT NULL CHECK (cantidad > 0),
  PRIMARY KEY (pedido_id, producto_id),
  FOREIGN KEY (pedido_id)   REFERENCES pedido (id),
  FOREIGN KEY (producto_id) REFERENCES producto (id)
) CHARACTER SET utf8mb4;

INSERT INTO producto(nombre, precio) VALUES
  ("Teclado", 12.50),
  ("Ratón", 8.75),
  ("Teclado", 26.25);

INSERT INTO cliente (id, nombre, email) VALUES
  (1, 'Ana',   'ana@ejemplo.es'),
  (2, 'Luis',  'luis@ejemplo.es'),
  (3, 'Marta', 'marta@ejemplo.es');

INSERT INTO pedido (id, cliente_id, fecha) VALUES
  (1, 1, '2026-10-01 10:00:00'),
  (2, 1, '2026-10-03 18:30:00'),
  (3, 2, '2026-10-05 12:15:00');

INSERT INTO linea_pedido (pedido_id, producto_id, cantidad) VALUES
  (1, 1, 1),   -- pedido 1: 1 teclado
  (1, 2, 2),   -- pedido 1: 2 ratones
  (2, 3, 1),   -- pedido 2: 1 monitor
  (3, 1, 2);   -- pedido 3: 2 teclados
