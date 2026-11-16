package es.dwes.ut03;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO (objeto de acceso a datos) de {@link Producto}: <b>qué</b> se puede hacer con los
 * productos guardados, sin decir <b>cómo</b> se guardan.
 *
 * <p>Quien use esta interfaz no sabe si detrás hay MySQL o una lista en memoria. Si algo falla,
 * lanza {@link AccesoDatosException}, nunca {@code SQLException}.
 */
public interface ProductoDao {

    /** Todos los productos, ordenados por id. */
    List<Producto> listar();

    /** El producto con ese id, o vacío si no existe. */
    Optional<Producto> buscarPorId(int id);

    /** Los productos cuyo nombre es exactamente el indicado, ordenados por id. */
    List<Producto> buscarPorNombre(String nombre);

    /** Guarda un producto nuevo y devuelve el id que se le asigna. */
    int insertar(String nombre, BigDecimal precio);

    /** Cambia el precio. Devuelve {@code true} si el producto existía. */
    boolean actualizarPrecio(int id, BigDecimal nuevoPrecio);

    /** Borra el producto. Devuelve {@code true} si existía. */
    boolean eliminar(int id);
}