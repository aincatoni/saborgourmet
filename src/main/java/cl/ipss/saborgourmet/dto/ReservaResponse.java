package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.model.Turno;

import java.time.LocalDate;

/**
 * Datos de salida de una reserva.
 *
 * @param id               identificador único
 * @param clienteId        identificador del cliente
 * @param mesaId           identificador de la mesa
 * @param fecha            fecha de la reserva
 * @param turno            turno horario
 * @param cantidadPersonas cantidad de comensales
 * @param estado           estado actual ({@code ACTIVA} o {@code CANCELADA})
 * @param observaciones    notas opcionales
 */
public record ReservaResponse(
        Long id,
        Long clienteId,
        Long mesaId,
        LocalDate fecha,
        Turno turno,
        Integer cantidadPersonas,
        EstadoReserva estado,
        String observaciones
) {
    /**
     * Convierte una entidad {@link Reserva} a su DTO de respuesta.
     *
     * @param reserva entidad origen
     * @return DTO de respuesta
     */
    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getClienteId(),
                reserva.getMesaId(),
                reserva.getFecha(),
                reserva.getTurno(),
                reserva.getCantidadPersonas(),
                reserva.getEstado(),
                reserva.getObservaciones()
        );
    }
}
