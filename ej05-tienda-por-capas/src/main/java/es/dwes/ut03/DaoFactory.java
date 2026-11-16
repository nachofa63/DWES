package es.dwes.ut03;

public final class DaoFactory {

    private DaoFactory() { }

    public static ProductoDao productoDao(String modo, String url, String usuario, String clave) {
        if (modo == null) {
            throw new IllegalArgumentException("Falta el modo del DAO");
        }
        return switch (modo) {
            case "memoria" -> new ProductoDaoMemoria();
            case "jdbc" -> new ProductoDaoJdbc(url, usuario, clave);
            default -> throw new IllegalArgumentException("Modo de DAO desconocido: " + modo);
        };
    }
}