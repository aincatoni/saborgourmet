package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.MesaRequest;
import cl.ipss.saborgourmet.dto.MesaResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de negocio para la gestión de mesas y consulta de disponibilidad.
 */
@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ReservaRepository reservaRepository;

    /**
     * Construye el servicio con los repositorios necesarios.
     *
     * @param mesaRepository    repositorio de mesas
     * @param reservaRepository repositorio de reservas
     */
    public MesaService(MesaRepository mesaRepository, ReservaRepository reservaRepository) {
        this.mesaRepository = mesaRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * Lista todas las mesas registradas.
     *
     * @return lista de mesas
     */
    public List<MesaResponse> listar() {
        return mesaRepository.findAll().stream()
                .map(MesaResponse::from)
                .toList();
    }

    /**
     * Obtiene una mesa por su identificador.
     *
     * @param id identificador de la mesa
     * @return datos de la mesa
     */
    public MesaResponse obtenerPorId(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mesa no encontrada con id: " + id));
        return MesaResponse.from(mesa);
    }

    /**
     * Consulta las mesas disponibles para una fecha, turno y cantidad de personas.
     * Devuelve mesas activas con capacidad suficiente y sin reserva activa en ese turno,
     * ordenadas por capacidad ascendente.
     *
     * @param fecha    fecha consultada
     * @param turno    turno horario
     * @param personas cantidad mínima de comensales
     * @return lista de mesas disponibles ordenadas por capacidad
     */
    public List<MesaResponse> disponibles(LocalDate fecha, Turno turno, int personas) {
        return mesaRepository.findAll().stream()
                .filter(Mesa::isActiva)
                .filter(m -> m.getCapacidad() >= personas)
                .filter(m -> !reservaRepository.existsActivaByMesaFechaTurno(m.getId(), fecha, turno, null))
                .sorted(Comparator.comparingInt(Mesa::getCapacidad))
                .map(MesaResponse::from)
                .toList();
    }

    /**
     * Crea una nueva mesa validando que su número sea único (R9).
     *
     * @param request datos de la mesa
     * @return mesa creada
     */
    public MesaResponse crear(MesaRequest request) {
        // R9: Número de mesa único
        if (mesaRepository.existsByNumero(request.numero())) {
            throw new ConflictoException("Ya existe una mesa con el número: " + request.numero());
        }

        boolean activa = request.activa() == null || request.activa();
        Mesa nueva = new Mesa(
                null,
                request.numero(),
                request.capacidad(),
                request.ubicacion() != null ? request.ubicacion().trim() : "",
                activa
        );
        return MesaResponse.from(mesaRepository.save(nueva));
    }

    /**
     * Actualiza una mesa existente validando unicidad de número (R9).
     *
     * @param id      identificador de la mesa
     * @param request nuevos datos de la mesa
     * @return mesa actualizada
     */
    public MesaResponse actualizar(Long id, MesaRequest request) {
        Mesa existente = mesaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mesa no encontrada con id: " + id));

        // R9: Número de mesa único excluyendo la propia mesa
        if (mesaRepository.existsByNumeroAndIdNot(request.numero(), id)) {
            throw new ConflictoException("Ya existe otra mesa con el número: " + request.numero());
        }

        existente.setNumero(request.numero());
        existente.setCapacidad(request.capacidad());
        existente.setUbicacion(request.ubicacion() != null ? request.ubicacion().trim() : "");
        if (request.activa() != null) {
            existente.setActiva(request.activa());
        }

        return MesaResponse.from(mesaRepository.save(existente));
    }

    /**
     * Elimina una mesa si no posee reservas activas (R10).
     *
     * @param id identificador de la mesa
     */
    public void eliminar(Long id) {
        if (mesaRepository.findById(id).isEmpty()) {
            throw new RecursoNoEncontradoException("Mesa no encontrada con id: " + id);
        }
        // R10: No se elimina una mesa con reservas activas
        if (reservaRepository.existsActivaByMesaId(id)) {
            throw new ConflictoException("No se puede eliminar la mesa " + id + " porque tiene reservas activas");
        }
        mesaRepository.deleteById(id);
    }
}
