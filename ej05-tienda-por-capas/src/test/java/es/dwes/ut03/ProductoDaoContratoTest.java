package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <b>Contrato</b> de {@link ProductoDao}: lo que tiene que cumplir CUALQUIER implementación.
 * Se escribe una sola vez; {@link ProductoDaoJdbcTest} y {@link ProductoDaoMemoriaTest} lo
 * heredan y lo ejecutan cada una con su implementación.
 *
 * <p>Cada subclase devuelve un DAO con estos tres productos: 1 Teclado 24.90, 2 Ratón 12.50 y
 * 3 Monitor 139.00.
 */
abstract class ProductoDaoContratoTest {

    protected ProductoDao dao;

    /** Una implementación nueva, con los tres productos de partida. */
    protected abstract ProductoDao crearDaoConTresProductos() throws Exception;

    @BeforeEach
    void preparar() throws Exception {
        dao = crearDaoConTresProductos();
    }

    private static void assertPrecio(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Se esperaba el precio " + esperado + " pero llegó " + real);
    }

    @Test
    void listarDevuelveTodosLosProductosOrdenadosPorId() {
        List<Producto> productos = dao.listar();

        assertEquals(3, productos.size());
        assertEquals("Teclado", productos.get(0).nombre());
        assertEquals("Monitor", productos.get(2).nombre());
        assertPrecio("24.90", productos.get(0).precio());
    }

    @Test
    void buscarPorIdDevuelveElProductoSiExiste() {
        Producto p = dao.buscarPorId(2).orElseThrow();

        assertEquals(2, p.id());
        assertEquals("Ratón", p.nombre());
        assertPrecio("12.50", p.precio());
    }

    @Test
    void buscarPorIdDevuelveVacioSiNoExiste() {
        assertTrue(dao.buscarPorId(999).isEmpty());
    }

    @Test
    void buscarPorNombreEncuentraLaCoincidenciaExacta() {
        List<Producto> encontrados = dao.buscarPorNombre("Monitor");

        assertEquals(1, encontrados.size());
        assertEquals(3, encontrados.get(0).id());
    }

    @Test
    void buscarPorNombreSinCoincidenciasDevuelveListaVacia() {
        assertTrue(dao.buscarPorNombre("Impresora").isEmpty());
    }

    @Test
    void insertarDevuelveElIdNuevoYElProductoQuedaGuardado() {
        int id = dao.insertar("Auriculares", new BigDecimal("39.95"));

        assertEquals(4, id);
        assertEquals(4, dao.listar().size());
        Producto guardado = dao.buscarPorId(id).orElseThrow();
        assertEquals("Auriculares", guardado.nombre());
        assertPrecio("39.95", guardado.precio());
    }

    @Test
    void actualizarPrecioCambiaElPrecioYDevuelveTrue() {
        assertTrue(dao.actualizarPrecio(1, new BigDecimal("19.90")));

        assertPrecio("19.90", dao.buscarPorId(1).orElseThrow().precio());
        assertEquals("Teclado", dao.buscarPorId(1).orElseThrow().nombre(), "El nombre no debe cambiar");
    }

    @Test
    void actualizarPrecioDeUnProductoInexistenteDevuelveFalse() {
        assertFalse(dao.actualizarPrecio(999, new BigDecimal("1.00")));
    }

    @Test
    void eliminarBorraElProductoYDevuelveTrue() {
        assertTrue(dao.eliminar(2));

        assertTrue(dao.buscarPorId(2).isEmpty());
        assertEquals(2, dao.listar().size());
    }

    @Test
    void eliminarUnProductoInexistenteDevuelveFalse() {
        assertFalse(dao.eliminar(999));
        assertEquals(3, dao.listar().size());
    }
}
