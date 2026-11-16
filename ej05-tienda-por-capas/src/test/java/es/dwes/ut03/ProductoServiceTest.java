package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la lógica de negocio <b>sin base de datos</b>: el servicio recibe un
 * {@link ProductoDaoMemoria}. No hay ningún {@code import java.sql} en este fichero.
 */
class ProductoServiceTest {

    private ProductoDao dao;
    private ProductoService servicio;

    @BeforeEach
    void preparar() {
        dao = new ProductoDaoMemoria();
        dao.insertar("Teclado", new BigDecimal("24.90"));
        dao.insertar("Ratón", new BigDecimal("12.50"));
        dao.insertar("Monitor", new BigDecimal("139.00"));
        servicio = new ProductoService(dao);
    }

    private static void assertPrecio(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Se esperaba " + esperado + " pero llegó " + real);
    }

    // ---- Consultas ------------------------------------------------------

    @Test
    void elCatalogoDevuelveDtosConElPrecioConIva() {
        List<ProductoDto> catalogo = servicio.catalogo();

        assertEquals(3, catalogo.size());
        assertEquals("Teclado", catalogo.get(0).nombre());
        assertPrecio("30.13", catalogo.get(0).precioConIva());
    }

    @Test
    void elCatalogoDeUnaTiendaVaciaEsUnaListaVacia() {
        assertTrue(new ProductoService(new ProductoDaoMemoria()).catalogo().isEmpty());
    }

    @Test
    void obtenerDevuelveElDtoDelProducto() {
        ProductoDto dto = servicio.obtener(3);

        assertEquals("Monitor", dto.nombre());
        assertPrecio("168.19", dto.precioConIva());
    }

    @Test
    void obtenerUnProductoInexistenteLanzaReglaDeNegocio() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.obtener(999));
    }

    // ---- Alta -----------------------------------------------------------

    @Test
    void darDeAltaGuardaElProductoYDevuelveSuId() {
        int id = servicio.darDeAlta("Auriculares", new BigDecimal("39.95"));

        assertEquals(4, id);
        assertEquals("Auriculares", dao.buscarPorId(id).orElseThrow().nombre());
    }

    @Test
    void darDeAltaGuardaElNombreSinEspaciosSobrantes() {
        int id = servicio.darDeAlta("  Auriculares  ", new BigDecimal("39.95"));

        assertEquals("Auriculares", dao.buscarPorId(id).orElseThrow().nombre());
    }

    @Test
    void darDeAltaRechazaNombresNulosOEnBlanco() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeAlta(null, new BigDecimal("5.00")));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeAlta("   ", new BigDecimal("5.00")));
        assertEquals(3, dao.listar().size(), "No debe guardarse nada");
    }

    @Test
    void darDeAltaRechazaPreciosNulosCeroONegativos() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeAlta("Cable", null));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeAlta("Cable", BigDecimal.ZERO));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeAlta("Cable", new BigDecimal("-1.00")));
        assertEquals(3, dao.listar().size(), "No debe guardarse nada");
    }

    @Test
    void darDeAltaRechazaUnNombreRepetidoSinDistinguirMayusculas() {
        ReglaDeNegocioException minusculas = assertThrows(ReglaDeNegocioException.class,
                () -> servicio.darDeAlta("teclado", new BigDecimal("10.00")));
        assertEquals("Ya existe un producto llamado teclado", minusculas.getMessage());

        ReglaDeNegocioException conEspacios = assertThrows(ReglaDeNegocioException.class,
                () -> servicio.darDeAlta(" TECLADO ", new BigDecimal("10.00")));
        assertEquals("Ya existe un producto llamado TECLADO", conEspacios.getMessage(),
                "El mensaje lleva el nombre ya sin espacios sobrantes");
        assertEquals(3, dao.listar().size(), "No debe guardarse nada");
    }

    // ---- Descuento ------------------------------------------------------

    @Test
    void aplicarDescuentoRebajaElPrecioConDosDecimales() {
        servicio.aplicarDescuento(1, 10);   // 24.90 x 0.90 = 22.41

        assertPrecio("22.41", dao.buscarPorId(1).orElseThrow().precio());
    }

    @Test
    void aplicarDescuentoRedondeaHalfUp() {
        servicio.aplicarDescuento(2, 15);   // 12.50 x 0.85 = 10.625 -> 10.63

        assertPrecio("10.63", dao.buscarPorId(2).orElseThrow().precio());
    }

    @Test
    void aplicarDescuentoAceptaLosLimites1Y90() {
        servicio.aplicarDescuento(3, 1);    // 139.00 x 0.99 = 137.61
        servicio.aplicarDescuento(1, 90);   // 24.90 x 0.10 = 2.49

        assertPrecio("137.61", dao.buscarPorId(3).orElseThrow().precio());
        assertPrecio("2.49", dao.buscarPorId(1).orElseThrow().precio());
    }

    @Test
    void aplicarDescuentoRechazaPorcentajesFueraDeRango() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.aplicarDescuento(1, 0));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.aplicarDescuento(1, 91));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.aplicarDescuento(1, -5));
        assertPrecio("24.90", dao.buscarPorId(1).orElseThrow().precio());
    }

    @Test
    void aplicarDescuentoAUnProductoInexistenteLanzaReglaDeNegocio() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.aplicarDescuento(999, 10));
    }
}
