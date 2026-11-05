package es.dwes.ut02;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Prueba manual contra el MySQL del contenedor (los tests usan H2, no esto).
 * Necesita las variables de entorno DB_URL, DB_USER y DB_PASSWORD (ver .env.example).
 */
public class App {

    public static void main(String[] args) throws SQLException {
        ProductoCrud crud = new ProductoCrud(
                System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD"));

        System.out.println("Productos al empezar:");
        crud.listar().forEach(System.out::println);

        int id = crud.insertar("Auriculares", new BigDecimal("39.95"));
        System.out.println("Insertado con id " + id);

        crud.actualizarPrecio(id, new BigDecimal("34.95"));
        System.out.println("Tras actualizar: " + crud.buscarPorId(id));

        crud.eliminar(id);
        System.out.println("Tras eliminar: " + crud.buscarPorId(id));
    }
}
