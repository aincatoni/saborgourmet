package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Mesa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la colección de mesas sobre {@link JsonDataStore}.
 */
@Repository
public class MesaRepository {

    private final JsonDataStore store;

    /**
     * Construye el repositorio con el almacén JSON inyectado.
     *
     * @param store almacén de datos JSON
     */
    public MesaRepository(JsonDataStore store) {
        this.store = store;
    }

    /**
     * Lista todas las mesas registradas.
     *
     * @return lista inmutable de mesas
     */
    public List<Mesa> findAll() {
        return List.copyOf(store.getDatos().getMesas());
    }

    /**
     * Busca una mesa por su identificador.
     *
     * @param id identificador de la mesa
     * @return {@link Optional} con la mesa si existe
     */
    public Optional<Mesa> findById(Long id) {
        return store.getDatos().getMesas().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    /**
     * Guarda una mesa nueva (asignando ID) o actualiza una existente y persiste en JSON.
     *
     * @param mesa mesa a guardar
     * @return mesa guardada con su ID
     */
    public Mesa save(Mesa mesa) {
        List<Mesa> mesas = store.getDatos().getMesas();
        if (mesa.getId() == null) {
            long nuevoId = mesas.stream()
                    .mapToLong(Mesa::getId)
                    .max()
                    .orElse(0L) + 1;
            mesa.setId(nuevoId);
            mesas.add(mesa);
        } else {
            for (int i = 0; i < mesas.size(); i++) {
                if (mesas.get(i).getId().equals(mesa.getId())) {
                    mesas.set(i, mesa);
                    break;
                }
            }
        }
        store.guardar();
        return mesa;
    }

    /**
     * Elimina una mesa por su identificador y persiste el cambio en JSON.
     *
     * @param id identificador de la mesa a eliminar
     * @return {@code true} si se eliminó algún registro
     */
    public boolean deleteById(Long id) {
        boolean eliminado = store.getDatos().getMesas().removeIf(m -> m.getId().equals(id));
        if (eliminado) {
            store.guardar();
        }
        return eliminado;
    }

    /**
     * Verifica si existe una mesa con el número indicado.
     *
     * @param numero número de mesa
     * @return {@code true} si ya existe
     */
    public boolean existsByNumero(Integer numero) {
        return store.getDatos().getMesas().stream()
                .anyMatch(m -> m.getNumero().equals(numero));
    }

    /**
     * Verifica si existe otra mesa distinta al ID dado con el mismo número.
     *
     * @param numero número de mesa
     * @param id     identificador a excluir
     * @return {@code true} si existe otra mesa con ese número
     */
    public boolean existsByNumeroAndIdNot(Integer numero, Long id) {
        return store.getDatos().getMesas().stream()
                .anyMatch(m -> !m.getId().equals(id) && m.getNumero().equals(numero));
    }
}
