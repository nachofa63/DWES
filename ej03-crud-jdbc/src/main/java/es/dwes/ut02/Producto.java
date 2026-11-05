package es.dwes.ut02;

import java.math.BigDecimal;

/** Una fila de la tabla {@code producto}. */
public record Producto(int id, String nombre, BigDecimal precio) { }
