package es.dwes.ut03;

/**
 * Capa de presentación mínima: pide datos al servicio y los muestra por consola.
 *
 * <p>Con {@code DAO_MODO=memoria} (por defecto) funciona sin base de datos; con {@code jdbc}
 * necesita DB_URL, DB_USER y DB_PASSWORD (ver .env.example) y la base del contenedor opcional
 * de esta carpeta ({@code docker compose up -d --wait}).
 */
public class App {

    public static void main(String[] args) {
        String modo = System.getenv().getOrDefault("DAO_MODO", "memoria");
        ClienteDao dao = DaoFactory.clienteDao(modo,
                System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));
        ClienteService servicio = new ClienteService(dao);

        System.out.println("Implementación del DAO: " + dao.getClass().getSimpleName());
        if (modo.equals("memoria")) {
            servicio.registrar("Ana", "Ana@Ejemplo.es");
            servicio.registrar("Luis", "luis@ejemplo.es");
        }

        System.out.println("--- Clientes (email oculto) ---");
        servicio.listar().forEach(c -> System.out.println(c.nombre() + ": " + c.emailOculto()));

        try {
            servicio.registrar("Otra Ana", "ANA@ejemplo.es");
        } catch (ReglaDeNegocioException e) {
            System.out.println("Rechazado: " + e.getMessage());
        }
    }
}
