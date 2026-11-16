package es.dwes.ut03;

/**
 * Fallo al acceder a los datos. Es de la capa de datos pero <b>no menciona JDBC</b>: así quien
 * llama al DAO no depende de cómo se guardan los datos (BD, memoria, fichero...).
 */
public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
