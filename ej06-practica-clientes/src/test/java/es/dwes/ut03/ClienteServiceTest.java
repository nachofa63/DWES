package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Reglas de negocio de los clientes, sin base de datos (DAO en memoria). */
class ClienteServiceTest {

    private ClienteDao dao;
    private ClienteService servicio;

    @BeforeEach
    void preparar() {
        dao = new ClienteDaoMemoria();
        dao.insertar("Ana", "ana@ejemplo.es");
        servicio = new ClienteService(dao);
    }

    @Test
    void registrarGuardaAlClienteYDevuelveSuId() {
        int id = servicio.registrar("Luis", "luis@ejemplo.es");

        assertEquals(2, id);
        assertEquals("Luis", dao.buscarPorEmail("luis@ejemplo.es").orElseThrow().nombre());
    }

    @Test
    void registrarNormalizaElNombreYElEmail() {
        servicio.registrar("  Marta  ", "  MARTA@Ejemplo.ES ");

        Cliente guardado = dao.buscarPorEmail("marta@ejemplo.es").orElseThrow();
        assertEquals("Marta", guardado.nombre());
    }

    @Test
    void registrarRechazaNombresNulosOEnBlanco() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar(null, "a@b.es"));
        assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar("  ", "a@b.es"));
        assertEquals(1, dao.listar().size());
    }

    @Test
    void registrarRechazaEmailsInvalidos() {
        for (String malo : new String[] {null, "", "sin-arroba", "dos@@ejemplo.es", "con espacio@ejemplo.es",
                "@ejemplo.es", "ana@", "ana@sinpunto"}) {
            assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar("Pepe", malo),
                    "Debería rechazar el email: " + malo);
        }
        assertEquals(1, dao.listar().size());
    }

    @Test
    void registrarRechazaUnEmailYaRegistradoAunqueCambienLasMayusculas() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.registrar("Otra", "ANA@ejemplo.es"));
        assertEquals(1, dao.listar().size());
    }

    @Test
    void listarDevuelveDtosConElEmailOculto() {
        var lista = servicio.listar();

        assertEquals(1, lista.size());
        assertEquals("Ana", lista.get(0).nombre());
        assertEquals("a***@ejemplo.es", lista.get(0).emailOculto());
    }

    @Test
    void buscarPorEmailNormalizaLaBusqueda() {
        assertEquals("Ana", servicio.buscarPorEmail(" ANA@ejemplo.es ").nombre());
    }

    @Test
    void buscarPorEmailInexistenteLanzaReglaDeNegocio() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.buscarPorEmail("nadie@ejemplo.es"));
    }

    @Test
    void darDeBajaBorraAlCliente() {
        servicio.darDeBaja("ana@ejemplo.es");

        assertEquals(0, dao.listar().size());
    }

    @Test
    void darDeBajaDeUnClienteInexistenteLanzaReglaDeNegocio() {
        assertThrows(ReglaDeNegocioException.class, () -> servicio.darDeBaja("nadie@ejemplo.es"));
    }
}
