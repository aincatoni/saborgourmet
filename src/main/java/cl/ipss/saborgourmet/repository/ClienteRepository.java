package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Cliente;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la colección de clientes sobre {@link JsonDataStore}.
 */
@Repository
public class ClienteRepository {

    private final JsonDataStore store;

    /**
     * Construye el repositorio con el almacén JSON inyectado.
     *
     * @param store almacén de datos JSON
     */
    public ClienteRepository(JsonDataStore store) {
        this.store = store;
    }

    /**
     * Lista todos los clientes registrados.
     *
     * @return lista inmutable de clientes
     */
    public List<Cliente> findAll() {
        return List.copyOf(store.getDatos().getClientes());
    }

    /**
     * Busca un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return {@link Optional} con el cliente si existe
     */
    public Optional<Cliente> findById(Long id) {
        return store.getDatos().getClientes().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    /**
     * Guarda un cliente nuevo (asignando ID) o actualiza uno existente y persiste en JSON.
     *
     * @param cliente cliente a guardar
     * @return cliente guardado con su ID
     */
    public Cliente save(Cliente cliente) {
        List<Cliente> clientes = store.getDatos().getClientes();
        if (cliente.getId() == null) {
            long nuevoId = clientes.stream()
                    .mapToLong(Cliente::getId)
                    .max()
                    .orElse(0L) + 1;
            cliente.setId(nuevoId);
            clientes.add(cliente);
        } else {
            for (int i = 0; i < clientes.size(); i++) {
                if (clientes.get(i).getId().equals(cliente.getId())) {
                    clientes.set(i, cliente);
                    break;
                }
            }
        }
        store.guardar();
        return cliente;
    }

    /**
     * Elimina un cliente por su identificador y persiste el cambio en JSON.
     *
     * @param id identificador del cliente a eliminar
     * @return {@code true} si se eliminó algún registro
     */
    public boolean deleteById(Long id) {
        boolean eliminado = store.getDatos().getClientes().removeIf(c -> c.getId().equals(id));
        if (eliminado) {
            store.guardar();
        }
        return eliminado;
    }

    /**
     * Verifica si existe un cliente con el RUT indicado.
     *
     * @param rut RUT a verificar
     * @return {@code true} si ya existe
     */
    public boolean existsByRut(String rut) {
        return store.getDatos().getClientes().stream()
                .anyMatch(c -> c.getRut().equalsIgnoreCase(rut));
    }

    /**
     * Verifica si existe otro cliente distinto al ID dado con el mismo RUT.
     *
     * @param rut RUT a verificar
     * @param id  identificador a excluir
     * @return {@code true} si existe otro cliente con ese RUT
     */
    public boolean existsByRutAndIdNot(String rut, Long id) {
        return store.getDatos().getClientes().stream()
                .anyMatch(c -> !c.getId().equals(id) && c.getRut().equalsIgnoreCase(rut));
    }

    /**
     * Verifica si existe un cliente con el correo indicado.
     *
     * @param email correo a verificar
     * @return {@code true} si ya existe
     */
    public boolean existsByEmail(String email) {
        return store.getDatos().getClientes().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Verifica si existe otro cliente distinto al ID dado con el mismo correo.
     *
     * @param email correo a verificar
     * @param id    identificador a excluir
     * @return {@code true} si existe otro cliente con ese correo
     */
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return store.getDatos().getClientes().stream()
                .anyMatch(c -> !c.getId().equals(id) && c.getEmail().equalsIgnoreCase(email));
    }
}
