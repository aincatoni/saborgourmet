package cl.ipss.saborgourmet.model;

import java.time.LocalDate;

/**
 * Representa una reserva de mesa realizada por un cliente para una fecha y turno.
 * Las referencias a {@link Cliente} y {@link Mesa} se almacenan únicamente por ID.
 */
public class Reserva {

    private Long id;
    private Long clienteId;
    private Long mesaId;
    private LocalDate fecha;
    private Turno turno;
    private Integer cantidadPersonas;
    private EstadoReserva estado;
    private String observaciones;

    /**
     * Constructor por defecto requerido por Jackson.
     */
    public Reserva() {
    }

    /**
     * Crea una reserva con todos sus atributos.
     *
     * @param id               identificador único
     * @param clienteId        identificador del cliente
     * @param mesaId           identificador de la mesa
     * @param fecha            fecha de la reserva
     * @param turno            turno horario
     * @param cantidadPersonas número de comensales
     * @param estado           estado de la reserva
     * @param observaciones    notas opcionales
     */
    public Reserva(Long id, Long clienteId, Long mesaId, LocalDate fecha, Turno turno,
                   Integer cantidadPersonas, EstadoReserva estado, String observaciones) {
        this.id = id;
        this.clienteId = clienteId;
        this.mesaId = mesaId;
        this.fecha = fecha;
        this.turno = turno;
        this.cantidadPersonas = cantidadPersonas;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public Integer getCantidadPersonas() {
        return cantidadPersonas;
    }

    public void setCantidadPersonas(Integer cantidadPersonas) {
        this.cantidadPersonas = cantidadPersonas;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
