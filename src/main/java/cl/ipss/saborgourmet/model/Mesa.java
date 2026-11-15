package cl.ipss.saborgourmet.model;

/**
 * Representa una mesa física del restaurante Sabor Gourmet.
 */
public class Mesa {

    private Long id;
    private Integer numero;
    private Integer capacidad;
    private String ubicacion;
    private boolean activa = true;

    /**
     * Constructor por defecto requerido por Jackson.
     */
    public Mesa() {
    }

    /**
     * Crea una mesa con todos sus atributos.
     *
     * @param id        identificador único
     * @param numero    número visible de la mesa
     * @param capacidad cantidad máxima de comensales
     * @param ubicacion sector del restaurante (Salón, Terraza, etc.)
     * @param activa    indica si la mesa está habilitada para reservas
     */
    public Mesa(Long id, Integer numero, Integer capacidad, String ubicacion, boolean activa) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
        this.activa = activa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
