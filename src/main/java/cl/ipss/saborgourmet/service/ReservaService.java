package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.ReservaRequest;
import cl.ipss.saborgourmet.dto.ReservaResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.exception.ReglaNegocioException;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de negocio para la gestión de reservas (reglas R2–R8 y R11).
 */
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteRepository clienteRepository;
    private final MesaRepository mesaRepository;

    /**
     * Construye el servicio con los repositorios necesarios.
     *
     * @param reservaRepository repositorio de reservas
     * @param clienteRepository repositorio de clientes
     * @param mesaRepository    repositorio de mesas
     */
    public ReservaService(ReservaRepository reservaRepository,
                          ClienteRepository clienteRepository,
                          MesaRepository mesaRepository) {
        this.reservaRepository = reservaRepository;
        this.clienteRepository = clienteRepository;
        this.mesaRepository = mesaRepository;
    }

    /**
     * Lista reservas aplicando filtros opcionales de fecha y estado.
     *
     * @param fecha  fecha exacta (opcional)
     * @param estado estado de la reserva (opcional)
     * @return lista de reservas
     */
    public List<ReservaResponse> listar(LocalDate fecha, EstadoReserva estado) {
        return reservaRepository.findByFiltros(fecha, estado).stream()
                .map(ReservaResponse::from)
                .toList();
    }

    /**
     * Obtiene una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return datos de la reserva
     */
    public ReservaResponse obtenerPorId(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id: " + id));
        return ReservaResponse.from(reserva);
    }

    /**
     * Crea una nueva reserva en estado {@code ACTIVA} validando las reglas R2–R6.
     *
     * @param request datos de la reserva
     * @return reserva creada
     */
    public ReservaResponse crear(ReservaRequest request) {
        validarReglasReserva(request, null);

        Reserva nueva = new Reserva(
                null,
                request.clienteId(),
                request.mesaId(),
                request.fecha(),
                request.turno(),
                request.cantidadPersonas(),
                EstadoReserva.ACTIVA,
                request.observaciones() != null ? request.observaciones().trim() : ""
        );
        return ReservaResponse.from(reservaRepository.save(nueva));
    }

    /**
     * Modifica una reserva activa existente validando las reglas R2–R6 y R8.
     *
     * @param id      identificador de la reserva
     * @param request nuevos datos de la reserva
     * @return reserva actualizada
     */
    public ReservaResponse actualizar(Long id, ReservaRequest request) {
        Reserva existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id: " + id));

        // R8: Solo se modifica una reserva ACTIVA
        if (existente.getEstado() != EstadoReserva.ACTIVA) {
            throw new ConflictoException("Solo se puede modificar una reserva activa");
        }

        validarReglasReserva(request, id);

        existente.setClienteId(request.clienteId());
        existente.setMesaId(request.mesaId());
        existente.setFecha(request.fecha());
        existente.setTurno(request.turno());
        existente.setCantidadPersonas(request.cantidadPersonas());
        existente.setObservaciones(request.observaciones() != null ? request.observaciones().trim() : "");

        return ReservaResponse.from(reservaRepository.save(existente));
    }

    /**
     * Cancela una reserva activa pasando su estado a {@code CANCELADA} (R7 y R11).
     * La reserva no se elimina del archivo y la mesa queda disponible nuevamente.
     *
     * @param id identificador de la reserva
     * @return reserva actualizada con estado {@code CANCELADA}
     */
    public ReservaResponse cancelar(Long id) {
        Reserva existente = reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada con id: " + id));

        // R7: Solo se cancela una reserva ACTIVA
        if (existente.getEstado() != EstadoReserva.ACTIVA) {
            throw new ConflictoException("La reserva " + id + " ya se encuentra cancelada");
        }

        // R11: Cambia estado a CANCELADA y persiste sin borrar
        existente.setEstado(EstadoReserva.CANCELADA);
        return ReservaResponse.from(reservaRepository.save(existente));
    }

    private void validarReglasReserva(ReservaRequest request, Long excluirReservaId) {
        // R3: La fecha no puede ser anterior a hoy
        if (request.fecha().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de la reserva no puede ser anterior a hoy");
        }

        // R2: El cliente y la mesa deben existir
        if (clienteRepository.findById(request.clienteId()).isEmpty()) {
            throw new RecursoNoEncontradoException("Cliente no encontrado con id: " + request.clienteId());
        }
        Mesa mesa = mesaRepository.findById(request.mesaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Mesa no encontrada con id: " + request.mesaId()));

        // R4: La mesa debe estar activa
        if (!mesa.isActiva()) {
            throw new ConflictoException("La mesa " + mesa.getNumero() + " no está activa");
        }

        // R5: Cantidad de personas <= capacidad de la mesa
        if (request.cantidadPersonas() > mesa.getCapacidad()) {
            throw new ReglaNegocioException(
                    "La cantidad de personas (" + request.cantidadPersonas()
                            + ") excede la capacidad de la mesa (" + mesa.getCapacidad() + ")"
            );
        }

        // R6: Sin superposición de reservas activas para la misma mesa, fecha y turno
        if (reservaRepository.existsActivaByMesaFechaTurno(
                request.mesaId(), request.fecha(), request.turno(), excluirReservaId)) {
            throw new ConflictoException(
                    "La mesa " + request.mesaId() + " ya tiene una reserva activa el "
                            + request.fecha() + " en el turno " + request.turno()
            );
        }
    }
}
