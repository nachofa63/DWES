package es.dwes.ut03;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lógica de negocio de los clientes. Recibe un {@link ClienteDao} (la interfaz).
 *
 * <p>Reglas:
 * <ul>
 *   <li>El nombre no puede ser nulo ni estar en blanco (se guarda sin espacios sobrantes).</li>
 *   <li>El email no puede ser nulo y debe tener la forma {@code algo@dominio.ext}: sin espacios,
 *       una sola arroba y un punto en el dominio. Se guarda sin espacios sobrantes y en minúsculas.</li>
 *   <li>No puede haber dos clientes con el mismo email (aunque se escriba con otras mayúsculas).</li>
 *   <li>Cualquier incumplimiento lanza {@link ReglaDeNegocioException} y no se guarda nada.</li>
 * </ul>
 */
public class ClienteService {

    private static final Pattern PATRON_EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s.]+(?:\\.[^@\\s.]+)+$");

    private final ClienteDao dao;

    /**
     * Construye el servicio inyectando su dependencia de acceso a datos.
     *
     * @param dao implementación de {@link ClienteDao}
     */
    public ClienteService(ClienteDao dao) {
        this.dao = dao;
    }

    /**
     * Registra un cliente y devuelve su id.
     *
     * @param nombre nombre del cliente
     * @param email correo electrónico del cliente
     * @return identificador asignado al nuevo cliente
     * @throws ReglaDeNegocioException si el nombre o el email no son válidos o si el email ya existe
     */
    public int registrar(String nombre, String email) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioException("El nombre del cliente no puede estar vacío");
        }
        String emailNormalizado = normalizarYValidarEmail(email);

        if (dao.buscarPorEmail(emailNormalizado).isPresent()) {
            throw new ReglaDeNegocioException("Ya hay un cliente con ese email");
        }

        return dao.insertar(nombre.strip(), emailNormalizado);
    }

    /**
     * Todos los clientes como DTO (con el email oculto).
     *
     * @return lista de clientes con datos minimizados
     */
    public List<ClienteDto> listar() {
        return dao.listar().stream()
                .map(ClienteMapper::aDto)
                .toList();
    }

    /**
     * Un cliente como DTO, buscado por su email (se normaliza igual que al registrar).
     *
     * @param email correo electrónico a buscar
     * @return DTO del cliente encontrado
     * @throws ReglaDeNegocioException si el email es inválido o no existe ningún cliente con él
     */
    public ClienteDto buscarPorEmail(String email) {
        String emailNormalizado = normalizarYValidarEmail(email);
        return dao.buscarPorEmail(emailNormalizado)
                .map(ClienteMapper::aDto)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe un cliente con ese email"));
    }

    /**
     * Da de baja al cliente con ese email.
     *
     * @param email correo electrónico del cliente a eliminar
     * @throws ReglaDeNegocioException si el email es inválido o no existe ningún cliente con él
     */
    public void darDeBaja(String email) {
        String emailNormalizado = normalizarYValidarEmail(email);
        Cliente cliente = dao.buscarPorEmail(emailNormalizado)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe un cliente con ese email"));
        dao.eliminar(cliente.id());
    }

    private static String normalizarYValidarEmail(String email) {
        if (email == null) {
            throw new ReglaDeNegocioException("El email no puede ser nulo");
        }
        String limpio = email.strip().toLowerCase(Locale.ROOT);
        if (!PATRON_EMAIL.matcher(limpio).matches()) {
            throw new ReglaDeNegocioException("El formato del email no es válido");
        }
        return limpio;
    }
}