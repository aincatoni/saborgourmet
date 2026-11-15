package cl.ipss.saborgourmet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor raíz serializado en el archivo JSON de persistencia.
 */
public class BaseDatos {

    private List<Cliente> clientes = new ArrayList<>();
    private List<Mesa> mesas = new ArrayList<>();
    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Constructor por defecto con listas vacías.
     */
    public BaseDatos() {
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes != null ? clientes : new ArrayList<>();
    }

    public List<Mesa> getMesas() {
        return mesas;
    }

    public void setMesas(List<Mesa> mesas) {
        this.mesas = mesas != null ? mesas : new ArrayList<>();
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas != null ? reservas : new ArrayList<>();
    }
}
