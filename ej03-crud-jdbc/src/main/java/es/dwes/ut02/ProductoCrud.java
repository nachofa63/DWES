package es.dwes.ut02;

import java.math.BigDecimal;
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
 * CRUD de la tabla {@code producto} con JDBC.
 *
 * <p>Reglas del ejercicio:
 * <ul>
 *   <li>Cada método abre su conexión con {@code try-with-resources}.</li>
 *   <li>Los valores van siempre como parámetros {@code ?}: nunca se concatenan en el SQL.</li>
 * </ul>
 *
 * <p>didactico: abrir una conexión por método es una simplificación; el pool y las
 * transacciones se ven en el apunte 10 y el patrón DAO en la UT3.
 */
public class ProductoCrud {

    private final String url;
    private final String usuario;
    private final String clave;

    public ProductoCrud(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Inserta un producto y devuelve el id que le ha asignado la base de datos. */
   public int insertar(Producto producto) {
    String sql = "INSERT INTO producto (nombre, precio) VALUES (?, ?)";
    try (Connection conn = conexion.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, producto.getNombre());
        ps.setBigDecimal(2, producto.getPrecio());
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return -1;
}

    /** Devuelve el producto con ese id, o vacío si no existe. */
  public Optional<Producto> buscarPorId(int id) {
    String sql = "SELECT id, nombre, precio FROM producto WHERE id = ?";
    try (Connection conn = conexion.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("precio")));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return Optional.empty();
}

    /** Devuelve todos los productos, ordenados por id. */
   public List<Producto> listar() {
    List<Producto> lista = new ArrayList<>();
    String sql = "SELECT id, nombre, precio FROM producto ORDER BY id";
    try (Connection conn = conexion.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            lista.add(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("precio")));
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return lista;
}

    /** Devuelve los productos cuyo nombre es exactamente el indicado. */
    public List<Producto> buscarPorNombre(String filtro) {
    List<Producto> lista = new ArrayList<>();
    String sql = "SELECT id, nombre, precio FROM producto WHERE nombre LIKE ?";
    try (Connection conn = conexion.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, "%" + filtro + "%");
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("precio")));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return lista;
}

    /** Cambia el precio. Devuelve {@code true} si existía el producto. */
    public boolean actualizarPrecio(int id, BigDecimal nuevoPrecio) {
    String sql = "UPDATE producto SET precio = ? WHERE id = ?";
    try (Connection conn = conexion.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setBigDecimal(1, nuevoPrecio);
        ps.setInt(2, id);
        int filasAfectadas = ps.executeUpdate();
        return filasAfectadas > 0;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}

    /** Borra el producto. Devuelve {@code true} si existía. */
    public boolean eliminar(int id) {
    String sql = "DELETE FROM producto WHERE id = ?";
    try (Connection conn = conexion.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        int filasAfectadas = ps.executeUpdate();
        return filasAfectadas > 0;
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
