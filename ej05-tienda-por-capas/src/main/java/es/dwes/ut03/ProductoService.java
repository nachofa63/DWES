package es.dwes.ut03;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ProductoService {

    static final BigDecimal IVA = new BigDecimal("0.21");

    private final ProductoDao dao;

    public ProductoService(ProductoDao dao) {
        this.dao = dao;
    }

    public List<ProductoDto> catalogo() {
        return dao.listar().stream()
                .map(producto -> ProductoMapper.aDto(producto, IVA))
                .toList();
    }

    public ProductoDto obtener(int id) {
        Producto producto = dao.buscarPorId(id)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el producto con id " + id));
        return ProductoMapper.aDto(producto, IVA);
    }

    public int darDeAlta(String nombre, BigDecimal precio) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioException("El nombre del producto no puede estar vacío");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new ReglaDeNegocioException("El precio debe ser mayor que cero");
        }

        String nombreLimpio = nombre.strip();

        for (Producto p : dao.listar()) {
            if (p.nombre().equalsIgnoreCase(nombreLimpio)) {
                throw new ReglaDeNegocioException("Ya existe un producto llamado " + nombreLimpio);
            }
        }

        return dao.insertar(nombreLimpio, precio);
    }

    public void aplicarDescuento(int id, int porcentaje) {
        if (porcentaje < 1 || porcentaje > 90) {
            throw new ReglaDeNegocioException("El porcentaje de descuento debe estar entre 1 y 90");
        }

        Producto producto = dao.buscarPorId(id)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el producto con id " + id));

        BigDecimal factor = BigDecimal.valueOf(100 - porcentaje);
        BigDecimal nuevoPrecio = producto.precio()
                .multiply(factor)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        dao.actualizarPrecio(id, nuevoPrecio);
    }
}