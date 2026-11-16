package es.dwes.ut03;

/** Se ha intentado algo que las reglas de la tienda no permiten (nombre vacío, descuento absurdo...). */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
