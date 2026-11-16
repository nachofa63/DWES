package es.dwes.ut03;

/**
 * DTO para mostrar clientes. No lleva el {@code id} y <b>no expone el email completo</b>
 * (minimización de datos): {@code ana@ejemplo.es} se muestra como {@code a***@ejemplo.es}.
 */
public record ClienteDto(String nombre, String emailOculto) { }
