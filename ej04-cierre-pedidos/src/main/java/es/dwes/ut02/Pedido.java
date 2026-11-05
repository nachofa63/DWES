package es.dwes.ut02;

import java.time.LocalDateTime;

/** Una fila de la tabla {@code pedido}. */
public record Pedido(int id, int clienteId, LocalDateTime fecha) { }
