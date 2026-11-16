-- Se ejecuta solo la primera vez que se crea el contenedor.
-- Sin esta línea, las tildes se guardan mal (doble codificación).
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS cliente (
  id     INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  -- utf8mb4_0900_bin: el `=` y el UNIQUE distinguen mayúsculas, tildes y espacios finales, igual
  -- que H2 y que el DAO en memoria. Con la colación por defecto de MySQL, 'Ana@x.es' y 'ana@x.es'
  -- contarían como el mismo email y el contrato de tests (ClienteDaoContratoTest) dejaría de
  -- cumplirse con MySQL real.
  email  VARCHAR(150) COLLATE utf8mb4_0900_bin NOT NULL UNIQUE
) CHARACTER SET utf8mb4;

INSERT INTO cliente (nombre, email) VALUES
  ('Ana',    'ana@ejemplo.es'),
  ('Luis',   'luis@ejemplo.es'),
  ('Marta',  'marta@ejemplo.es'),
  ('Álvaro', 'alvaro@ejemplo.es');
