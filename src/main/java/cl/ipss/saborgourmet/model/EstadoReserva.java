package cl.ipss.saborgourmet.model;

/**
 * Estados posibles de una reserva en el sistema.
 */
public enum EstadoReserva {
    /** Reserva vigente que ocupa la mesa en la fecha y turno indicados. */
    ACTIVA,
    /** Reserva cancelada; no ocupa la mesa pero se conserva como historial. */
    CANCELADA
}
