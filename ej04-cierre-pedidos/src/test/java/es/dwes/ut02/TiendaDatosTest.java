package es.dwes.ut02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests con H2 en memoria (modo MySQL). Mismo esquema y datos que el ej02. */
class TiendaDatosTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 20, 9, 30);

    private String url;
    private TiendaDatos tienda;

    @BeforeEach
    void prepararBaseDeDatosLimpia() throws SQLException {
        url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (Connection c = DriverManager.getConnection(url, "sa", "");
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE producto (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  nombre VARCHAR(100) NOT NULL,
                  precio DECIMAL(8, 2) NOT NULL CHECK (precio >= 0))""");
            st.execute("""
                CREATE TABLE cliente (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  nombre VARCHAR(100) NOT NULL,
                  email VARCHAR(150) NOT NULL UNIQUE)""");
            st.execute("""
                CREATE TABLE pedido (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  cliente_id INT NOT NULL,
                  fecha TIMESTAMP NOT NULL,
                  FOREIGN KEY (cliente_id) REFERENCES cliente (id))""");
            st.execute("""
                CREATE TABLE linea_pedido (
                  pedido_id INT NOT NULL,
                  producto_id INT NOT NULL,
                  cantidad INT NOT NULL CHECK (cantidad > 0),
                  PRIMARY KEY (pedido_id, producto_id),
                  FOREIGN KEY (pedido_id) REFERENCES pedido (id),
                  FOREIGN KEY (producto_id) REFERENCES producto (id))""");
            st.execute("INSERT INTO producto (nombre, precio) VALUES "
                    + "('Teclado', 24.90), ('Ratón', 12.50), ('Monitor', 139.00)");
            st.execute("INSERT INTO cliente (nombre, email) VALUES "
                    + "('Ana', 'ana@ejemplo.es'), ('Luis', 'luis@ejemplo.es'), ('Marta', 'marta@ejemplo.es')");
            st.execute("INSERT INTO pedido (cliente_id, fecha) VALUES "
                    + "(1, '2026-10-01 10:00:00'), (1, '2026-10-03 18:30:00'), (2, '2026-10-05 12:15:00')");
            st.execute("INSERT INTO linea_pedido (pedido_id, producto_id, cantidad) VALUES "
                    + "(1, 1, 1), (1, 2, 2), (2, 3, 1), (3, 1, 2)");
        }
        tienda = new TiendaDatos(url, "sa", "");
    }

    /** Cuenta filas con SQL directo, sin pasar por el código del alumnado. */
    private int contar(String tabla) throws SQLException {
        try (Connection c = DriverManager.getConnection(url, "sa", "");
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void assertImporte(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Se esperaba " + esperado + " pero llegó " + real);
    }

    // ---- Clientes -------------------------------------------------------

    @Test
    void crearClienteDevuelveElIdGenerado() throws SQLException {
        assertEquals(4, tienda.crearCliente("Pablo", "pablo@ejemplo.es"));
        assertEquals(4, contar("cliente"));
    }

    @Test
    void crearClienteConEmailRepetidoLoRechazaLaBaseDeDatos() {
        assertThrows(SQLException.class, () -> tienda.crearCliente("Otra Ana", "ana@ejemplo.es"));
    }

    @Test
    void buscarClientePorEmailDevuelveElCliente() throws SQLException {
        Cliente c = tienda.buscarClientePorEmail("luis@ejemplo.es").orElseThrow();

        assertEquals(2, c.id());
        assertEquals("Luis", c.nombre());
    }

    @Test
    void buscarClientePorEmailDevuelveVacioSiNoExiste() throws SQLException {
        assertTrue(tienda.buscarClientePorEmail("nadie@ejemplo.es").isEmpty());
    }

    @Test
    void buscarClientePorEmailNoEsVulnerableAInyeccionSql() throws SQLException {
        assertTrue(tienda.buscarClientePorEmail("ana@ejemplo.es").isPresent(),
                "La búsqueda normal debe funcionar antes de atacarla");

        assertTrue(tienda.buscarClientePorEmail("x' OR '1'='1").isEmpty(),
                "La entrada se ha tratado como SQL: usa PreparedStatement con '?'");
    }

    // ---- Pedidos --------------------------------------------------------

    @Test
    void crearPedidoConLineasGuardaElPedidoYTodasSusLineas() throws SQLException {
        Map<Integer, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1, 2);   // 2 teclados
        lineas.put(3, 1);   // 1 monitor

        int pedidoId = tienda.crearPedidoConLineas(3, FECHA, lineas);

        assertEquals(4, pedidoId);
        assertEquals(4, contar("pedido"));
        assertEquals(6, contar("linea_pedido"));
        assertImporte("188.80", tienda.totalPedido(pedidoId));   // 2*24.90 + 139.00
    }

    @Test
    void crearPedidoConLineasConProductoInexistenteNoDejaNada() throws SQLException {
        Map<Integer, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1, 1);     // válida
        lineas.put(999, 1);   // el producto 999 no existe -> falla la FK

        assertThrows(SQLException.class, () -> tienda.crearPedidoConLineas(3, FECHA, lineas));

        assertEquals(3, contar("pedido"), "El pedido debe haberse deshecho (rollback)");
        assertEquals(4, contar("linea_pedido"), "La línea válida también debe deshacerse");
    }

    @Test
    void crearPedidoConLineasConCantidadCeroNoDejaNada() throws SQLException {
        Map<Integer, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1, 1);     // válida
        lineas.put(2, 0);     // el CHECK (cantidad > 0) la rechaza

        assertThrows(SQLException.class, () -> tienda.crearPedidoConLineas(3, FECHA, lineas));

        assertEquals(3, contar("pedido"));
        assertEquals(4, contar("linea_pedido"));
    }

    @Test
    void crearPedidoParaUnClienteInexistenteLoRechazaLaBaseDeDatos() {
        assertThrows(SQLException.class,
                () -> tienda.crearPedidoConLineas(999, FECHA, Map.of(1, 1)));
    }

    @Test
    void pedidosDeClienteDevuelveSoloLosDeEseCliente() throws SQLException {
        List<Pedido> deAna = tienda.pedidosDeCliente("ana@ejemplo.es");

        assertEquals(2, deAna.size());
        assertEquals(1, deAna.get(0).id());
        assertEquals(LocalDateTime.of(2026, 10, 1, 10, 0), deAna.get(0).fecha());
        assertEquals(1, tienda.pedidosDeCliente("luis@ejemplo.es").size());
        assertTrue(tienda.pedidosDeCliente("marta@ejemplo.es").isEmpty());
    }

    @Test
    void totalPedidoSumaCantidadPorPrecio() throws SQLException {
        assertImporte("49.90", tienda.totalPedido(1));    // 1*24.90 + 2*12.50
        assertImporte("139.00", tienda.totalPedido(2));
        assertImporte("49.80", tienda.totalPedido(3));    // 2*24.90
    }

    @Test
    void totalDeUnPedidoInexistenteEsCero() throws SQLException {
        assertImporte("0", tienda.totalPedido(999));
    }

    // ---- Cancelar -------------------------------------------------------

    @Test
    void cancelarPedidoBorraElPedidoYSusLineas() throws SQLException {
        assertTrue(tienda.cancelarPedido(1));

        assertEquals(2, contar("pedido"));
        assertEquals(2, contar("linea_pedido"), "Las 2 líneas del pedido 1 deben desaparecer");
        assertEquals(1, tienda.pedidosDeCliente("ana@ejemplo.es").size());
    }

    @Test
    void cancelarUnPedidoInexistenteDevuelveFalseYNoBorraNada() throws SQLException {
        assertFalse(tienda.cancelarPedido(999));

        assertEquals(3, contar("pedido"));
        assertEquals(4, contar("linea_pedido"));
    }
}
