package es.dwes.ut03;

import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;

/**
 * Reto: pasa el contrato con tu propia implementación guardada en un fichero.
 *
 * <p>Para usar este test, cópialo a {@code src/test/java/es/dwes/ut03/} y crea la clase
 * {@code ClienteDaoFichero} en {@code src/main/java/es/dwes/ut03/}, con un constructor que
 * reciba el {@link Path} del fichero. Los 7 tests del contrato deben salir en verde.
 */
class ClienteDaoFicheroTest extends ClienteDaoContratoTest {

    @TempDir
    Path carpeta;

    @Override
    protected ClienteDao crearDaoConDosClientes() {
        ClienteDaoFichero fichero = new ClienteDaoFichero(carpeta.resolve("clientes.txt"));
        fichero.insertar("Ana", "ana@ejemplo.es");
        fichero.insertar("Luis", "luis@ejemplo.es");
        return fichero;
    }
}
