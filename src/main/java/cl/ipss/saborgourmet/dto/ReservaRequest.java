package cl.ipss.saborgourmet.dto;

import cl.ipss.saborgourmet.model.Turno;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Datos de entrada para crear o modificar una reserva.
 *
 * @param clienteId        identificador del cliente (obligatorio)
 * @param mesaId           identificador de la mesa (obligatorio)
 * @param fecha            fecha de la reserva (obligatoria, hoy o posterior)
 * @param turno            turno horario (obligatorio)
 * @param cantidadPersonas cantidad de comensales (obligatoria, positiva)
 * @param observaciones    notas opcionales
 */
public record ReservaRequest(
        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        @NotNull(message = "El mesaId es obligatorio")
        Long mesaId,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha de la reserva no puede ser anterior a hoy")
        LocalDate fecha,

        @NotNull(message = "El turno es obligatorio")
        Turno turno,

        @NotNull(message = "La cantidad de personas es obligatoria")
        @Positive(message = "La cantidad de personas debe ser positiva")
        Integer cantidadPersonas,

        String observaciones
) {
}
