package es.dwes.ut03;

import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reto 2: pasa el contrato con tu propia implementación guardada en un fichero.
 *
 * <p>Para usar este test, cópialo a {@code src/test/java/es/dwes/ut03/} y crea la clase
 * {@code ProductoDaoFichero} en {@code src/main/java/es/dwes/ut03/}, con un constructor que
 * reciba el {@link Path} del fichero. Los 10 tests del contrato deben salir en verde.
 */
class ProductoDaoFicheroTest extends ProductoDaoContratoTest {

    @TempDir
    Path carpeta;

    @Override
    protected ProductoDao crearDaoConTresProductos() {
        ProductoDaoFichero fichero = new ProductoDaoFichero(carpeta.resolve("productos.txt"));
        fichero.insertar("Teclado", new BigDecimal("24.90"));
        fichero.insertar("Ratón", new BigDecimal("12.50"));
        fichero.insertar("Monitor", new BigDecimal("139.00"));
        return fichero;
    }
}
