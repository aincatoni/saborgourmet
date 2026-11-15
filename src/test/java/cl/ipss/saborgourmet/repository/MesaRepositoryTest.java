package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Mesa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MesaRepositoryTest {

    @TempDir
    Path tempDir;

    private Path rutaArchivo;
    private MesaRepository repository;

    @BeforeEach
    void setUp() {
        rutaArchivo = tempDir.resolve("datos.json");
        JsonDataStore store = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        store.inicializar();
        repository = new MesaRepository(store);
    }

    @Test
    void save_generaIdSecuencialMaxMasUnoYActualizaExistente() {
        Mesa m1 = repository.save(new Mesa(null, 1, 2, "Salón", true));
        assertThat(m1.getId()).isEqualTo(1L);

        Mesa m2 = repository.save(new Mesa(null, 2, 4, "Terraza", true));
        Mesa m3 = repository.save(new Mesa(null, 3, 6, "Privado", true));
        assertThat(m2.getId()).isEqualTo(2L);
        assertThat(m3.getId()).isEqualTo(3L);

        repository.deleteById(2L);

        Mesa m4 = repository.save(new Mesa(null, 4, 8, "Salón", true));
        assertThat(m4.getId()).isEqualTo(4L);

        m1.setCapacidad(4);
        repository.save(m1);

        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(1L)).get()
                .extracting(Mesa::getCapacidad)
                .isEqualTo(4);
    }

    @Test
    void saveYDeleteById_persistenInmediatamenteEnDisco() {
        Mesa guardada = repository.save(new Mesa(null, 10, 4, "Terraza", true));

        JsonDataStore storeLectura = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeLectura.inicializar();
        MesaRepository repoLectura = new MesaRepository(storeLectura);

        assertThat(repoLectura.findAll()).hasSize(1);
        assertThat(repoLectura.findById(guardada.getId())).get()
                .extracting(Mesa::getNumero)
                .isEqualTo(10);

        boolean eliminada = repository.deleteById(guardada.getId());
        assertThat(eliminada).isTrue();

        JsonDataStore storeTrasEliminar = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeTrasEliminar.inicializar();
        MesaRepository repoTrasEliminar = new MesaRepository(storeTrasEliminar);

        assertThat(repoTrasEliminar.findAll()).isEmpty();
    }

    @Test
    void existsByNumeroYExistsByNumeroAndIdNot_excluyePropioId() {
        Mesa m1 = repository.save(new Mesa(null, 10, 4, "Salón", true));
        Mesa m2 = repository.save(new Mesa(null, 20, 6, "Terraza", true));

        assertThat(repository.existsByNumero(10)).isTrue();
        assertThat(repository.existsByNumero(99)).isFalse();

        assertThat(repository.existsByNumeroAndIdNot(10, m1.getId())).isFalse();
        assertThat(repository.existsByNumeroAndIdNot(10, m2.getId())).isTrue();
    }
}
