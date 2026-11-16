package es.dwes.ut03;

import java.math.BigDecimal;

/**
 * Capa de presentación mínima: pide datos al servicio y los muestra por consola.
 *
 * <p>Con {@code DAO_MODO=memoria} funciona sin base de datos; con {@code jdbc} (por defecto)
 * necesita DB_URL, DB_USER y DB_PASSWORD (ver .env.example) y la base {@code tienda} del
 * contenedor de esta carpeta ({@code docker compose up -d --wait}).
 */
public class App {

    public static void main(String[] args) {
        String modo = System.getenv().getOrDefault("DAO_MODO", "jdbc");
        ProductoDao dao = DaoFactory.productoDao(modo,
                System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
        ProductoService servicio = new ProductoService(dao);

        System.out.println("Implementación del DAO: " + dao.getClass().getSimpleName());
        if (modo.equals("memoria")) {
            servicio.darDeAlta("Teclado", new BigDecimal("24.90"));
            servicio.darDeAlta("Ratón", new BigDecimal("12.50"));
        }

        System.out.println("--- Catálogo (precios con IVA) ---");
        servicio.catalogo().forEach(p -> System.out.println(p.nombre() + ": " + p.precioConIva() + " €"));

        try {
            servicio.darDeAlta("teclado", new BigDecimal("10.00"));
        } catch (ReglaDeNegocioException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
    }
}
