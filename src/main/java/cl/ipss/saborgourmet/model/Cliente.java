package cl.ipss.saborgourmet.model;

/**
 * Representa a un cliente registrado en el restaurante Sabor Gourmet.
 */
public class Cliente {

    private Long id;
    private String rut;
    private String nombre;
    private String email;
    private String telefono;

    /**
     * Constructor por defecto requerido por Jackson.
     */
    public Cliente() {
    }

    /**
     * Crea un cliente con todos sus atributos.
     *
     * @param id       identificador único
     * @param rut      RUT del cliente
     * @param nombre   nombre completo
     * @param email    correo electrónico
     * @param telefono número de teléfono
     */
    public Cliente(Long id, String rut, String nombre, String email, String telefono) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
