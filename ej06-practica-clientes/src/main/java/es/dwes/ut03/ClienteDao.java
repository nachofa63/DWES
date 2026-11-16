package es.dwes.ut03;

import java.util.List;
import java.util.Optional;

/** DAO de {@link Cliente}. Si algo falla lanza {@link AccesoDatosException}, nunca {@code SQLException}. */
public interface ClienteDao {

    /** Todos los clientes, ordenados por id. */
    List<Cliente> listar();

    /** El cliente con ese email (coincidencia exacta), o vacío si no existe. */
    Optional<Cliente> buscarPorEmail(String email);

    /**
     * Guarda un cliente nuevo y devuelve su id.
     *
     * @throws AccesoDatosException si ya existe otro cliente con ese email
     */
    int insertar(String nombre, String email);

    /** Borra el cliente. Devuelve {@code true} si existía. */
    boolean eliminar(int id);
}
