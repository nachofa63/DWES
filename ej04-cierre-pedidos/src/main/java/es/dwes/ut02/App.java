package es.dwes.ut02;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Prueba manual contra el MySQL del contenedor (los tests usan H2).
 * Necesita DB_URL, DB_USER y DB_PASSWORD (ver .env.example) y el esquema del ej02 ya creado.
 */
public class App {

    public static void main(String[] args) throws Exception {
        TiendaDatos tienda = new TiendaDatos(
                System.getenv("DB_URL"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD"));

        String email = "cierre-" + System.currentTimeMillis() + "@ejemplo.es";
        int clienteId = tienda.crearCliente("Cliente de cierre", email);
        System.out.println("Cliente creado: " + tienda.buscarClientePorEmail(email));

        int pedidoId = tienda.crearPedidoConLineas(clienteId, LocalDateTime.now(), Map.of(1, 2, 3, 1));
        System.out.println("Pedido " + pedidoId + ", total: " + tienda.totalPedido(pedidoId));
        System.out.println("Pedidos del cliente: " + tienda.pedidosDeCliente(email));

        System.out.println("Cancelado: " + tienda.cancelarPedido(pedidoId));
        System.out.println("Pedidos del cliente tras cancelar: " + tienda.pedidosDeCliente(email));
    }
}
