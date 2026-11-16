package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Hereda el contrato y usa H2 en memoria (modo MySQL). */
class ClienteDaoJdbcTest extends ClienteDaoContratoTest {

    @Override
    protected ClienteDao crearDaoConDosClientes() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (Connection c = DriverManager.getConnection(url, "sa", "");
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE cliente (
                  id     INT AUTO_INCREMENT PRIMARY KEY,
                  nombre VARCHAR(100) NOT NULL,
                  email  VARCHAR(150) NOT NULL UNIQUE
                )""");
            st.execute("INSERT INTO cliente (nombre, email) VALUES "
                    + "('Ana', 'ana@ejemplo.es'), ('Luis', 'luis@ejemplo.es')");
        }
        return new ClienteDaoJdbc(url, "sa", "");
    }

    @Test
    void buscarPorEmailNoEsVulnerableAInyeccionSql() {
        assertTrue(dao.buscarPorEmail("x' OR '1'='1").isEmpty());
    }

    @Test
    void siLaBaseDeDatosNoEstaDisponibleSeLanzaAccesoDatosException() {
        ClienteDao sinServidor = new ClienteDaoJdbc("jdbc:h2:tcp://localhost:1/no-existe", "sa", "");

        assertThrows(AccesoDatosException.class, sinServidor::listar);
    }
}
