package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Contrato de {@link ClienteDao}. Cada implementación devuelve un DAO con estos clientes:
 * 1 Ana (ana@ejemplo.es) y 2 Luis (luis@ejemplo.es).
 */
abstract class ClienteDaoContratoTest {

    protected ClienteDao dao;

    protected abstract ClienteDao crearDaoConDosClientes() throws Exception;

    @BeforeEach
    void preparar() throws Exception {
        dao = crearDaoConDosClientes();
    }

    @Test
    void listarDevuelveLosClientesOrdenadosPorId() {
        var clientes = dao.listar();

        assertEquals(2, clientes.size());
        assertEquals("Ana", clientes.get(0).nombre());
        assertEquals("Luis", clientes.get(1).nombre());
    }

    @Test
    void buscarPorEmailEncuentraAlClienteSiExiste() {
        Cliente c = dao.buscarPorEmail("luis@ejemplo.es").orElseThrow();

        assertEquals(2, c.id());
        assertEquals("Luis", c.nombre());
    }

    @Test
    void buscarPorEmailDevuelveVacioSiNoExiste() {
        assertTrue(dao.buscarPorEmail("nadie@ejemplo.es").isEmpty());
    }

    @Test
    void insertarDevuelveElIdNuevoYElClienteQuedaGuardado() {
        int id = dao.insertar("Marta", "marta@ejemplo.es");

        assertEquals(3, id);
        assertEquals("Marta", dao.buscarPorEmail("marta@ejemplo.es").orElseThrow().nombre());
        assertEquals(3, dao.listar().size());
    }

    @Test
    void insertarUnEmailRepetidoLanzaAccesoDatosException() {
        assertThrows(AccesoDatosException.class, () -> dao.insertar("Otra Ana", "ana@ejemplo.es"));

        assertEquals(2, dao.listar().size(), "No debe guardarse nada");
    }

    @Test
    void eliminarBorraAlClienteYDevuelveTrue() {
        assertTrue(dao.eliminar(1));

        assertTrue(dao.buscarPorEmail("ana@ejemplo.es").isEmpty());
        assertEquals(1, dao.listar().size());
    }

    @Test
    void eliminarUnClienteInexistenteDevuelveFalse() {
        assertFalse(dao.eliminar(999));
    }
}
