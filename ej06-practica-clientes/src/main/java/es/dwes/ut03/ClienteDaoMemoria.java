package es.dwes.ut03;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * {@link ClienteDao} en memoria, para probar el servicio sin base de datos.
 *
 * <p>Como en la BD (columna {@code email UNIQUE}), insertar un email que ya existe debe lanzar
 * {@link AccesoDatosException}, con {@code null} como causa (en memoria no ha fallado ninguna
 * {@code SQLException}). Los ids empiezan en 1 y no se reutilizan.
 */
public class ClienteDaoMemoria implements ClienteDao {

    private final Map<Integer, Cliente> clientes = new TreeMap<>();
    private int siguienteId = 1;

    @Override
    public List<Cliente> listar() {
        return new ArrayList<>(clientes.values());
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return clientes.values().stream()
                .filter(c -> c.email().equals(email))
                .findFirst();
    }

    @Override
    public int insertar(String nombre, String email) {
        if (buscarPorEmail(email).isPresent()) {
            throw new AccesoDatosException("Ya existe un cliente con ese email", null);
        }
        int id = siguienteId++;
        clientes.put(id, new Cliente(id, nombre, email));
        return id;
    }

    @Override
    public boolean eliminar(int id) {
        return clientes.remove(id) != null;
    }
}