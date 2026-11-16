package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DaoFactoryTest {

    @Test
    void modoMemoriaDevuelveElDaoEnMemoriaSinNecesitarDatosDeConexion() {
        ProductoDao dao = DaoFactory.productoDao("memoria", null, null, null);

        assertInstanceOf(ProductoDaoMemoria.class, dao);
    }

    @Test
    void modoJdbcDevuelveElDaoJdbc() {
        ProductoDao dao = DaoFactory.productoDao("jdbc", "jdbc:h2:mem:fabrica", "sa", "");

        assertInstanceOf(ProductoDaoJdbc.class, dao);
    }

    @Test
    void unModoDesconocidoSeRechazaConUnMensajeQueLoNombra() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> DaoFactory.productoDao("mongo", null, null, null));

        org.junit.jupiter.api.Assertions.assertTrue(e.getMessage().contains("mongo"),
                "El mensaje debe decir qué modo no se conoce");
    }

    @Test
    void unModoNuloTambienSeRechazaConIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> DaoFactory.productoDao(null, null, null, null));
    }
}
