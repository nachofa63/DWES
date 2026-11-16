package es.dwes.ut03;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class ProductoDaoMemoria implements ProductoDao {

    private final Map<Integer, Producto> productos = new TreeMap<>();
    private int siguienteId = 1;

    @Override
    public List<Producto> listar() {
        return new ArrayList<>(productos.values());
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {
        return Optional.ofNullable(productos.get(id));
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos.values()) {
            if (p.nombre().equals(nombre)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    @Override
    public int insertar(String nombre, BigDecimal precio) {
        int id = siguienteId++;
        productos.put(id, new Producto(id, nombre, precio));
        return id;
    }

    @Override
    public boolean actualizarPrecio(int id, BigDecimal nuevoPrecio) {
        Producto existente = productos.get(id);
        if (existente == null) {
            return false;
        }
        productos.put(id, new Producto(id, existente.nombre(), nuevoPrecio));
        return true;
    }

    @Override
    public boolean eliminar(int id) {
        return productos.remove(id) != null;
    }
}
