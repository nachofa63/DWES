package es.dwes.ut03;

/**
 * Fábrica de DAO: decide <b>qué implementación</b> se usa. El resto del programa pide un
 * {@link ClienteDao} y no sabe (ni le importa) cuál recibe.
 */
public final class DaoFactory {

    private DaoFactory() { }

    /**
     * Crea la implementación de {@link ClienteDao} correspondiente al modo indicado.
     *
     * @param modo {@code "jdbc"} o {@code "memoria"}
     * @param url URL de conexión JDBC (solo se usa con {@code "jdbc"})
     * @param usuario usuario de la base de datos (solo se usa con {@code "jdbc"})
     * @param clave contraseña de la base de datos (solo se usa con {@code "jdbc"})
     * @return instancia concreta de {@link ClienteDao}
     * @throws IllegalArgumentException si el modo es nulo o desconocido
     */
    public static ClienteDao clienteDao(String modo, String url, String usuario, String clave) {
        if (modo == null) {
            throw new IllegalArgumentException("Falta el modo del DAO");
        }
        return switch (modo) {
            case "memoria" -> new ClienteDaoMemoria();
            case "jdbc" -> new ClienteDaoJdbc(url, usuario, clave);
            default -> throw new IllegalArgumentException("Modo de DAO desconocido: " + modo);
        };
    }
}