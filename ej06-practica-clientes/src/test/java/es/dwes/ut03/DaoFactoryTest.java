package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DaoFactoryTest {

    @Test
    void modoMemoriaDevuelveElDaoEnMemoriaSinNecesitarDatosDeConexion() {
        ClienteDao dao = DaoFactory.clienteDao("memoria", null, null, null);

        assertInstanceOf(ClienteDaoMemoria.class, dao);
    }

    @Test
    void modoJdbcDevuelveElDaoJdbc() {
        ClienteDao dao = DaoFactory.clienteDao("jdbc", "jdbc:h2:mem:fabrica", "sa", "");

        assertInstanceOf(ClienteDaoJdbc.class, dao);
    }

    @Test
    void unModoDesconocidoSeRechazaConUnMensajeQueLoNombra() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DaoFactory.clienteDao("mongo", null, null, null));

        assertTrue(e.getMessage().contains("mongo"), "El mensaje debe decir qué modo no se conoce");
    }

    @Test
    void unModoNuloTambienSeRechazaConIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DaoFactory.clienteDao(null, null, null, null));
    }
}
