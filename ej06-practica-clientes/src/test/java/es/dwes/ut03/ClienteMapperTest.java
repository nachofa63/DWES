package es.dwes.ut03;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClienteMapperTest {

    @Test
    void ocultaElEmailManteniendoPrimeraLetraYDominio() {
        Cliente cliente = new Cliente(1, "Ana", "ana@ejemplo.es");

        ClienteDto dto = ClienteMapper.aDto(cliente);

        assertEquals("a***@ejemplo.es", dto.emailOculto());
    }

    @Test
    void casoLimiteParteLocalDeUnaSolaLetra() {
        Cliente cliente = new Cliente(2, "Alberto", "a@x.es");

        ClienteDto dto = ClienteMapper.aDto(cliente);

        assertEquals("a***@x.es", dto.emailOculto());
    }

    @Test
    void mantieneIntactoElNombreDelCliente() {
        Cliente cliente = new Cliente(3, "María José", "mjose@dominio.com");

        ClienteDto dto = ClienteMapper.aDto(cliente);

        assertEquals("María José", dto.nombre());
    }

    @Test
    void conservaSubdominiosCompletosAlOcultarElEmail() {
        Cliente cliente = new Cliente(4, "Juan", "juan@mail.empresa.com");

        ClienteDto dto = ClienteMapper.aDto(cliente);

        assertEquals("j***@mail.empresa.com", dto.emailOculto());
    }
}