-- Se ejecuta solo la primera vez que se crea el contenedor.
-- Sin esta línea, las tildes se guardan mal (doble codificación).
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS producto (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  -- utf8mb4_0900_bin: el `=` distingue mayúsculas, tildes y espacios finales ('Teclado' <> 'teclado' <>
  -- 'Teclado '), igual que H2 y que el DAO en memoria. Con la colación por defecto de MySQL no las
  -- distinguiría y el contrato de tests (ProductoDaoContratoTest) dejaría de cumplirse con MySQL real.
  -- (utf8mb4_bin, en cambio, ignora los espacios finales al comparar.)
  nombre VARCHAR(100) COLLATE utf8mb4_0900_bin NOT NULL,
  precio DECIMAL(8, 2) NOT NULL CHECK (precio >= 0)
) CHARACTER SET utf8mb4;

INSERT INTO producto (nombre, precio) VALUES
  ('Teclado',       24.90),
  ('Ratón',         12.50),
  ('Monitor',      139.00),
  ('Auriculares',   35.00),
  ('Webcam',        42.75),
  ('Alfombrilla',    6.99),
  ('Hub USB',       18.40),
  ('Cable HDMI',     9.95);
