package es.dwes.ut02;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso a los datos de la tienda: clientes, pedidos y líneas de pedido.
 *
 * <p>Reglas del ejercicio:
 * <ul>
 *   <li>Todas las sentencias usan parámetros {@code ?}: nunca se concatena SQL.</li>
 *   <li>Conexión, sentencia y resultado se abren con {@code try-with-resources}.</li>
 *   <li>Los métodos que tocan varias tablas usan <b>una transacción</b>: o se hace todo o nada.</li>
 * </ul>
 *
 * <p>didactico: una clase por tabla (patrón DAO) se verá en la UT3; aquí, todo junto
 * para centrarnos en JDBC.
 */
public class TiendaDatos {

    private final String url;
    private final String usuario;
    private final String clave;

    public TiendaDatos(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    // ---- Clientes -------------------------------------------------------

    /** Crea un cliente y devuelve su id. El email no puede repetirse (lanza SQLException). */
  public int crearCliente(String nombre, String email) {
    String sql = "INSERT INTO cliente (nombre, email) VALUES (?, ?)";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setString(1, nombre);
        ps.setString(2, email);
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return -1;
}

    /** Devuelve el cliente con ese email, o vacío si no existe. */
    public Optional<Cliente> buscarClientePorEmail(String email) {
    String sql = "SELECT id, nombre, email FROM cliente WHERE email = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("email")));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return Optional.empty();
}

    // ---- Pedidos --------------------------------------------------------

    /**
     * Crea un pedido con todas sus líneas y devuelve el id del pedido.
     *
     * @param lineas mapa {@code idProducto -> cantidad}
     * @throws SQLException si algo falla (producto inexistente, cantidad 0...).
     *         En ese caso <b>no debe quedar nada guardado</b>: ni el pedido ni ninguna línea.
     */
    public int crearPedidoConLineas(int clienteId, LocalDateTime fecha, Map<Integer, Integer> lineas) {
    String sqlPedido = "INSERT INTO pedido (cliente_id, fecha) VALUES (?, ?)";
    String sqlLinea = "INSERT INTO linea_pedido (pedido_id, producto_id, cantidad) VALUES (?, ?, ?)";
    
    Connection conn = null;
    try {
        conn = dataSource.getConnection();
        conn.setAutoCommit(false); // 1. Abrimos transacción
        
        int pedidoId = -1;
        try (PreparedStatement psPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            psPedido.setInt(1, clienteId);
            psPedido.setTimestamp(2, Timestamp.valueOf(fecha));
            psPedido.executeUpdate();
            try (ResultSet rs = psPedido.getGeneratedKeys()) {
                if (rs.next()) pedidoId = rs.getInt(1);
            }
        }
        
        try (PreparedStatement psLinea = conn.prepareStatement(sqlLinea)) {
            for (Map.Entry<Integer, Integer> entry : lineas.entrySet()) {
                psLinea.setInt(1, pedidoId);
                psLinea.setInt(2, entry.getKey());
                psLinea.setInt(3, entry.getValue());
                psLinea.addBatch();
            }
            psLinea.executeBatch();
        }
        
        conn.commit(); // 2. Todo correcto: confirmamos
        return pedidoId;
    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // 3. Falló: deshacemos todo
        }
        throw new RuntimeException(e);
    } finally {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
    /** Pedidos de un cliente, localizado por su email (JOIN), ordenados por id. */
    public List<Pedido> pedidosDeCliente(String email) {
    List<Pedido> lista = new ArrayList<>();
    String sql = "SELECT p.id, p.cliente_id, p.fecha FROM pedido p JOIN cliente c ON p.cliente_id = c.id WHERE c.email = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, email);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Pedido(rs.getInt("id"), rs.getInt("cliente_id"), rs.getTimestamp("fecha").toLocalDateTime()));
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return lista;
}

    /** Total del pedido: suma de cantidad x precio. Si no existe o no tiene líneas, cero. */
    public BigDecimal totalPedido(int pedidoId) {
    String sql = "SELECT SUM(lp.cantidad * pr.precio) AS total FROM linea_pedido lp JOIN producto pr ON lp.producto_id = pr.id WHERE lp.pedido_id = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, pedidoId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    return BigDecimal.ZERO;
}

    /**
     * Cancela un pedido: borra sus líneas y después el pedido, en una sola transacción.
     * Devuelve {@code true} si el pedido existía.
     */
    public boolean cancelarPedido(int pedidoId) {
    String sqlLineas = "DELETE FROM linea_pedido WHERE pedido_id = ?";
    String sqlPedido = "DELETE FROM pedido WHERE id = ?";
    
    Connection conn = null;
    try {
        conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        
        try (PreparedStatement psLineas = conn.prepareStatement(sqlLineas);
             PreparedStatement psPedido = conn.prepareStatement(sqlPedido)) {
            
            psLineas.setInt(1, pedidoId);
            psLineas.executeUpdate();
            
            psPedido.setInt(1, pedidoId);
            int filas = psPedido.executeUpdate();
            
            conn.commit();
            return filas > 0;
        }
    } catch (SQLException e) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        throw new RuntimeException(e);
    } finally {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
}
