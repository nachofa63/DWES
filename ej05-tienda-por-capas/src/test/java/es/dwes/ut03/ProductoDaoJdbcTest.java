package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Hereda el contrato y añade lo propio de JDBC. Usa H2 en memoria (modo MySQL): no hace falta
 * ningún servidor.
 */
class ProductoDaoJdbcTest extends ProductoDaoContratoTest {

    private String url;

    @Override
    protected ProductoDao crearDaoConTresProductos() throws Exception {
        // Una BD distinta por test, para que no dependan unos de otros.
        url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (Connection c = DriverManager.getConnection(url, "sa", "");
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE producto (
                  id     INT AUTO_INCREMENT PRIMARY KEY,
                  nombre VARCHAR(100)  NOT NULL,
                  precio DECIMAL(8, 2) NOT NULL CHECK (precio >= 0)
                )""");
            st.execute("INSERT INTO producto (nombre, precio) VALUES "
                    + "('Teclado', 24.90), ('Ratón', 12.50), ('Monitor', 139.00)");
        }
        return new ProductoDaoJdbc(url, "sa", "");
    }

    @Test
    void buscarPorNombreNoEsVulnerableAInyeccionSql() {
        assertTrue(dao.buscarPorNombre("x' OR '1'='1").isEmpty(),
                "El texto del usuario debe tratarse como dato, no como SQL");
    }

    @Test
    void unPrecioNegativoSeRechazaConAccesoDatosExceptionYNoConSqlException() {
        // La BD tiene CHECK (precio >= 0): salta una SQLException que el DAO debe envolver.
        AccesoDatosException e = assertThrows(AccesoDatosException.class,
                () -> dao.insertar("Roto", new BigDecimal("-1.00")));

        assertTrue(e.getCause() instanceof java.sql.SQLException, "La causa original debe conservarse");
    }

    @Test
    void siLaBaseDeDatosNoEstaDisponibleSeLanzaAccesoDatosException() {
        ProductoDao sinServidor = new ProductoDaoJdbc("jdbc:h2:tcp://localhost:1/no-existe", "sa", "");

        assertThrows(AccesoDatosException.class, sinServidor::listar);
    }
}
