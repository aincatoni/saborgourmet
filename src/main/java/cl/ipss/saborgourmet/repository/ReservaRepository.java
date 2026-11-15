package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.model.Turno;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la colección de reservas sobre {@link JsonDataStore}.
 */
@Repository
public class ReservaRepository {

    private final JsonDataStore store;

    /**
     * Construye el repositorio con el almacén JSON inyectado.
     *
     * @param store almacén de datos JSON
     */
    public ReservaRepository(JsonDataStore store) {
        this.store = store;
    }

    /**
     * Lista todas las reservas registradas.
     *
     * @return lista inmutable de reservas
     */
    public List<Reserva> findAll() {
        return List.copyOf(store.getDatos().getReservas());
    }

    /**
     * Lista reservas aplicando filtros opcionales por fecha y estado.
     *
     * @param fecha  fecha exacta (opcional, puede ser {@code null})
     * @param estado estado de la reserva (opcional, puede ser {@code null})
     * @return lista filtrada de reservas
     */
    public List<Reserva> findByFiltros(LocalDate fecha, EstadoReserva estado) {
        return store.getDatos().getReservas().stream()
                .filter(r -> fecha == null || r.getFecha().equals(fecha))
                .filter(r -> estado == null || r.getEstado() == estado)
                .toList();
    }

    /**
     * Busca una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return {@link Optional} con la reserva si existe
     */
    public Optional<Reserva> findById(Long id) {
        return store.getDatos().getReservas().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    /**
     * Guarda una reserva nueva (asignando ID) o actualiza una existente y persiste en JSON.
     *
     * @param reserva reserva a guardar
     * @return reserva guardada con su ID
     */
    public Reserva save(Reserva reserva) {
        List<Reserva> reservas = store.getDatos().getReservas();
        if (reserva.getId() == null) {
            long nuevoId = reservas.stream()
                    .mapToLong(Reserva::getId)
                    .max()
                    .orElse(0L) + 1;
            reserva.setId(nuevoId);
            reservas.add(reserva);
        } else {
            for (int i = 0; i < reservas.size(); i++) {
                if (reservas.get(i).getId().equals(reserva.getId())) {
                    reservas.set(i, reserva);
                    break;
                }
            }
        }
        store.guardar();
        return reserva;
    }

    /**
     * Verifica si un cliente tiene al menos una reserva activa.
     *
     * @param clienteId identificador del cliente
     * @return {@code true} si tiene reservas activas
     */
    public boolean existsActivaByClienteId(Long clienteId) {
        return store.getDatos().getReservas().stream()
                .anyMatch(r -> r.getClienteId().equals(clienteId)
                        && r.getEstado() == EstadoReserva.ACTIVA);
    }

    /**
     * Verifica si una mesa tiene al menos una reserva activa.
     *
     * @param mesaId identificador de la mesa
     * @return {@code true} si tiene reservas activas
     */
    public boolean existsActivaByMesaId(Long mesaId) {
        return store.getDatos().getReservas().stream()
                .anyMatch(r -> r.getMesaId().equals(mesaId)
                        && r.getEstado() == EstadoReserva.ACTIVA);
    }

    /**
     * Verifica si existe una reserva activa para la mesa, fecha y turno indicados,
     * excluyendo opcionalmente una reserva por su ID (útil al modificar).
     *
     * @param mesaId    identificador de la mesa
     * @param fecha     fecha de la reserva
     * @param turno     turno horario
     * @param excluirId ID de reserva a excluir, o {@code null} si es creación
     * @return {@code true} si existe superposición
     */
    public boolean existsActivaByMesaFechaTurno(Long mesaId, LocalDate fecha, Turno turno, Long excluirId) {
        return store.getDatos().getReservas().stream()
                .anyMatch(r -> r.getEstado() == EstadoReserva.ACTIVA
                        && r.getMesaId().equals(mesaId)
                        && r.getFecha().equals(fecha)
                        && r.getTurno() == turno
                        && (excluirId == null || !r.getId().equals(excluirId)));
    }
}
