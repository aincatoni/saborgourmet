package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.ClienteRequest;
import cl.ipss.saborgourmet.dto.ClienteResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio para la gestión de clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReservaRepository reservaRepository;

    /**
     * Construye el servicio con los repositorios necesarios.
     *
     * @param clienteRepository repositorio de clientes
     * @param reservaRepository repositorio de reservas
     */
    public ClienteService(ClienteRepository clienteRepository, ReservaRepository reservaRepository) {
        this.clienteRepository = clienteRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Lista todos los clientes registrados.
     *
     * @return lista de clientes
     */
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(ClienteResponse::from)
                .toList();
    }

    /**
     * Obtiene un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return datos del cliente
     */
    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));
        return ClienteResponse.from(cliente);
    }

    /**
     * Registra un nuevo cliente validando unicidad de RUT y email (R9).
     *
     * @param request datos del cliente
     * @return cliente creado
     */
    public ClienteResponse crear(ClienteRequest request) {
        // R9: RUT y email únicos
        if (clienteRepository.existsByRut(request.rut())) {
            throw new ConflictoException("Ya existe un cliente con el RUT: " + request.rut());
        }
        if (clienteRepository.existsByEmail(request.email())) {
            throw new ConflictoException("Ya existe un cliente con el email: " + request.email());
        }

        Cliente nuevo = new Cliente(
                null,
                request.rut().trim(),
                request.nombre().trim(),
                request.email().trim(),
                request.telefono().trim()
        );
        return ClienteResponse.from(clienteRepository.save(nuevo));
    }

    /**
     * Actualiza un cliente existente validando unicidad de RUT y email (R9).
     *
     * @param id      identificador del cliente
     * @param request nuevos datos del cliente
     * @return cliente actualizado
     */
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));

        // R9: RUT y email únicos excluyendo el propio registro
        if (clienteRepository.existsByRutAndIdNot(request.rut(), id)) {
            throw new ConflictoException("Ya existe otro cliente con el RUT: " + request.rut());
        }
        if (clienteRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictoException("Ya existe otro cliente con el email: " + request.email());
        }

        existente.setRut(request.rut().trim());
        existente.setNombre(request.nombre().trim());
        existente.setEmail(request.email().trim());
        existente.setTelefono(request.telefono().trim());

        return ClienteResponse.from(clienteRepository.save(existente));
    }

    /**
     * Elimina un cliente si no posee reservas activas (R10).
     *
     * @param id identificador del cliente
     */
    public void eliminar(Long id) {
        if (clienteRepository.findById(id).isEmpty()) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id: " + id);
        }
        // R10: No se elimina un cliente con reservas activas
        if (reservaRepository.existsActivaByClienteId(id)) {
            throw new ConflictoException("No se puede eliminar el cliente " + id + " porque tiene reservas activas");
        }
        clienteRepository.deleteById(id);
    }
}
