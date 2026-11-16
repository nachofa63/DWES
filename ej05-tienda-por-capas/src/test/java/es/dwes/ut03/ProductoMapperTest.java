package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductoMapperTest {

    private static final BigDecimal IVA = new BigDecimal("0.21");

    private static void assertPrecio(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Se esperaba " + esperado + " pero llegó " + real);
        assertEquals(2, real.scale(), "El precio debe tener exactamente 2 decimales");
    }

    @Test
    void copiaElNombre() {
        ProductoDto dto = ProductoMapper.aDto(new Producto(1, "Teclado", new BigDecimal("24.90")), IVA);

        assertEquals("Teclado", dto.nombre());
    }

    @Test
    void sumaElIvaYRedondeaADosDecimales() {
        // 24.90 x 1.21 = 30.129 -> 30.13
        assertPrecio("30.13", ProductoMapper.aDto(new Producto(1, "Teclado", new BigDecimal("24.90")), IVA).precioConIva());
        // 139.00 x 1.21 = 168.19
        assertPrecio("168.19", ProductoMapper.aDto(new Producto(3, "Monitor", new BigDecimal("139.00")), IVA).precioConIva());
    }

    @Test
    void elRedondeoEsHalfUp() {
        // 12.50 x 1.21 = 15.125 -> 15.13 (no 15.12)
        assertPrecio("15.13", ProductoMapper.aDto(new Producto(2, "Ratón", new BigDecimal("12.50")), IVA).precioConIva());
    }
}
