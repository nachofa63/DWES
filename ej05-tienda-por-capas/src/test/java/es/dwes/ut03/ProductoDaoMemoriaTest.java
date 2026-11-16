package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Hereda el contrato: la implementación en memoria debe comportarse igual que la de JDBC. */
class ProductoDaoMemoriaTest extends ProductoDaoContratoTest {

    @Override
    protected ProductoDao crearDaoConTresProductos() {
        ProductoDaoMemoria memoria = new ProductoDaoMemoria();
        memoria.insertar("Teclado", new BigDecimal("24.90"));
        memoria.insertar("Ratón", new BigDecimal("12.50"));
        memoria.insertar("Monitor", new BigDecimal("139.00"));
        return memoria;
    }

    @Test
    void laListaDevueltaEsUnaCopiaYNoPermiteTocarElEstadoInterno() {
        List<Producto> copia = dao.listar();
        try {
            copia.clear();
        } catch (UnsupportedOperationException e) {
            // También vale devolver una lista no modificable.
        }

        assertEquals(3, dao.listar().size());
    }

    @Test
    void losIdsNoSeReutilizanTrasBorrar() {
        dao.eliminar(3);

        assertEquals(4, dao.insertar("Auriculares", new BigDecimal("39.95")));
    }
}
