package es.dwes.ut03;

import java.math.BigDecimal;

/** Entidad: una fila de la tabla {@code producto}, tal cual está en la base de datos. */
public record Producto(int id, String nombre, BigDecimal precio) { }
