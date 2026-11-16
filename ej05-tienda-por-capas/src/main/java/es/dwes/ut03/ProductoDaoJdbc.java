package es.dwes.ut03;

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

public class ProductoDaoJdbc implements ProductoDao {

    private final String url;
    private final String usuario;
    private final String clave;

    public ProductoDaoJdbc(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    private static Producto aProducto(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getBigDecimal("precio"));
    }

    @Override
    public List<Producto> listar() {
        String sql = "SELECT id, nombre, precio FROM producto ORDER BY id";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Producto> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(aProducto(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los productos", e);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {
        String sql = "SELECT id, nombre, precio FROM producto WHERE id = ?";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(aProducto(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar el producto con id " + id, e);
        }
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, precio FROM producto WHERE nombre = ? ORDER BY id";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                List<Producto> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(aProducto(rs));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar productos por nombre", e);
        }
    }

    @Override
    public int insertar(String nombre, BigDecimal precio) {
        String sql = "INSERT INTO producto (nombre, precio) VALUES (?, ?)";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setBigDecimal(2, precio);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new AccesoDatosException("No se generó ningún ID al insertar el producto", null);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar el producto", e);
        }
    }

    @Override
    public boolean actualizarPrecio(int id, BigDecimal nuevoPrecio) {
        String sql = "UPDATE producto SET precio = ? WHERE id = ?";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, nuevoPrecio);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el precio del producto " + id, e);
        }
    }

    @Override
    public boolean eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo eliminar el producto " + id, e);
        }
    }
}