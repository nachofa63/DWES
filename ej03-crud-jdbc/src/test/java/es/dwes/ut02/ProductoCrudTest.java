package es.dwes.ut02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Los tests usan H2 en memoria (modo MySQL): no hace falta ningún servidor. */
class ProductoCrudTest {

    private String url;
    private ProductoCrud crud;

    @BeforeEach
    void prepararBaseDeDatosLimpia() throws SQLException {
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
        crud = new ProductoCrud(url, "sa", "");
    }

    private static void assertPrecio(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Se esperaba el precio " + esperado + " pero llegó " + real);
    }

    // ---- Read -----------------------------------------------------------

    @Test
    void listarDevuelveTodasLasFilasOrdenadasPorId() throws SQLException {
        List<Producto> productos = crud.listar();

        assertEquals(3, productos.size());
        assertEquals("Teclado", productos.get(0).nombre());
        assertEquals("Monitor", productos.get(2).nombre());
        assertPrecio("24.90", productos.get(0).precio());
    }

    @Test
    void buscarPorIdDevuelveElProductoSiExiste() throws SQLException {
        Producto p = crud.buscarPorId(2).orElseThrow();

        assertEquals(2, p.id());
        assertEquals("Ratón", p.nombre());
        assertPrecio("12.50", p.precio());
    }

    @Test
    void buscarPorIdDevuelveVacioSiNoExiste() throws SQLException {
        assertTrue(crud.buscarPorId(999).isEmpty());
    }

    @Test
    void buscarPorNombreEncuentraLaCoincidenciaExacta() throws SQLException {
        List<Producto> encontrados = crud.buscarPorNombre("Monitor");

        assertEquals(1, encontrados.size());
        assertEquals(3, encontrados.get(0).id());
    }

    @Test
    void buscarPorNombreNoEsVulnerableAInyeccionSql() throws SQLException {
        // Antes de atacar, la búsqueda normal debe funcionar (si no, el test pasaría "en vacío").
        assertEquals(1, crud.buscarPorNombre("Teclado").size());

        // Con SQL concatenado esto devolvería las 3 filas (apunte 09).
        List<Producto> encontrados = crud.buscarPorNombre("x' OR '1'='1");

        assertTrue(encontrados.isEmpty(),
                "La entrada se ha tratado como código SQL: usa PreparedStatement con '?'");
    }

    @Test
    void buscarPorNombreAceptaUnNombreConComilla() throws SQLException {
        int id = crud.insertar("Cable 1' USB", new BigDecimal("3.00"));

        assertEquals(id, crud.buscarPorNombre("Cable 1' USB").get(0).id());
    }

    // ---- Create ---------------------------------------------------------

    @Test
    void insertarDevuelveElIdGeneradoPorLaBaseDeDatos() throws SQLException {
        int id = crud.insertar("Auriculares", new BigDecimal("39.95"));

        assertEquals(4, id, "El primer id libre tras los 3 productos de partida es el 4");
    }

    @Test
    void insertarPersisteLaFila() throws SQLException {
        int id = crud.insertar("Auriculares", new BigDecimal("39.95"));

        Producto p = crud.buscarPorId(id).orElseThrow();
        assertEquals("Auriculares", p.nombre());
        assertPrecio("39.95", p.precio());
        assertEquals(4, crud.listar().size());
    }

    @Test
    void insertarUnPrecioNegativoLoRechazaLaBaseDeDatos() {
        // Integridad (apunte 04): el CHECK de la tabla protege aunque falle el código.
        assertThrows(SQLException.class,
                () -> crud.insertar("Roto", new BigDecimal("-5")));
    }

    // ---- Update ---------------------------------------------------------

    @Test
    void actualizarPrecioCambiaSoloEsaFila() throws SQLException {
        boolean existia = crud.actualizarPrecio(1, new BigDecimal("19.90"));

        assertTrue(existia);
        assertPrecio("19.90", crud.buscarPorId(1).orElseThrow().precio());
        assertPrecio("12.50", crud.buscarPorId(2).orElseThrow().precio());
    }

    @Test
    void actualizarPrecioDevuelveFalseSiElIdNoExiste() throws SQLException {
        assertFalse(crud.actualizarPrecio(999, new BigDecimal("1.00")));
    }

    // ---- Delete ---------------------------------------------------------

    @Test
    void eliminarBorraSoloEsaFila() throws SQLException {
        boolean existia = crud.eliminar(2);

        assertTrue(existia);
        assertTrue(crud.buscarPorId(2).isEmpty());
        assertEquals(2, crud.listar().size());
    }

    @Test
    void eliminarDevuelveFalseSiElIdNoExiste() throws SQLException {
        assertFalse(crud.eliminar(999));
        assertNotEquals(0, crud.listar().size(), "No debe borrar nada");
    }
}
