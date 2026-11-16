package es.dwes.ut03;

/** Convierte la entidad {@link Cliente} en el {@link ClienteDto} que se muestra. */
public final class ClienteMapper {

    private ClienteMapper() { }

    /**
     * El email se oculta: primera letra de la parte local, {@code ***} y el dominio completo.
     * {@code ana@ejemplo.es} pasa a {@code a***@ejemplo.es}.
     *
     * @param cliente entidad origen
     * @return DTO con el nombre intacto y el email parcialmente enmascarado
     */
    public static ClienteDto aDto(Cliente cliente) {
        String email = cliente.email();
        int arroba = email.indexOf('@');
        String emailOculto = email.charAt(0) + "***" + email.substring(arroba);
        return new ClienteDto(cliente.nombre(), emailOculto);
    }
}
