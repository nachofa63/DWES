package es.dwes.ut03;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link ClienteDao} con JDBC. Misma receta que {@code ProductoDaoJdbc} del ej05:
 * {@code try-with-resources}, parámetros {@code ?} y {@code SQLException} envuelta en
 * {@link AccesoDatosException}.
 */
public class ClienteDaoJdbc implements ClienteDao {

    private final String url;
    private final String usuario;
    private final String clave;

    /**
     * Crea un DAO respaldado por una base de datos JDBC.
     *
     * @param url URL de conexión JDBC
     * @param usuario usuario de la base de datos
     * @param clave contraseña de la base de datos
     */
    public ClienteDaoJdbc(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static Cliente aCliente(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("email"));
    }

    @Override
    public List<Cliente> listar() {
        String sql = "SELECT id, nombre, email FROM cliente ORDER BY id";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Cliente> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(aCliente(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los clientes", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        String sql = "SELECT id, nombre, email FROM cliente WHERE email = ?";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(aCliente(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar el cliente por email", e);
        }
    }

    @Override
    public int insertar(String nombre, String email) {
        String sql = "INSERT INTO cliente (nombre, email) VALUES (?, ?)";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new AccesoDatosException("No se generó ningún ID al insertar el cliente", null);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar el cliente", e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el cliente con id " + id, e);
        }
    }
}