package es.dwes.ut03;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ProductoMapper {

    private ProductoMapper() { }

    public static ProductoDto aDto(Producto producto, BigDecimal tipoIva) {
        BigDecimal multiplicador = BigDecimal.ONE.add(tipoIva);
        BigDecimal precioConIva = producto.precio()
                .multiply(multiplicador)
                .setScale(2, RoundingMode.HALF_UP);
        return new ProductoDto(producto.nombre(), precioConIva);
    }
}