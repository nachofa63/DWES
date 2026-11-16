package es.dwes.ut03;

import java.math.BigDecimal;

/**
 * DTO (objeto de transferencia de datos): lo que se entrega a quien <b>muestra</b> el catálogo.
 * Tiene la forma que necesita la pantalla, no la de la tabla: no lleva el {@code id} y el
 * precio ya incluye el IVA.
 */
public record ProductoDto(String nombre, BigDecimal precioConIva) { }
