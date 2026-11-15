package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteRepositoryTest {

    @TempDir
    Path tempDir;

    private Path rutaArchivo;
    private ClienteRepository repository;

    @BeforeEach
    void setUp() {
        rutaArchivo = tempDir.resolve("datos.json");
        JsonDataStore store = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        store.inicializar();
        repository = new ClienteRepository(store);
    }

    @Test
    void save_generaIdSecuencialMaxMasUnoYActualizaExistente() {
        Cliente primero = repository.save(new Cliente(null, "11111111-1", "Uno", "uno@mail.com", "+5691"));
        assertThat(primero.getId()).isEqualTo(1L);

        Cliente segundo = repository.save(new Cliente(null, "22222222-2", "Dos", "dos@mail.com", "+5692"));
        Cliente tercero = repository.save(new Cliente(null, "33333333-3", "Tres", "tres@mail.com", "+5693"));
        assertThat(segundo.getId()).isEqualTo(2L);
        assertThat(tercero.getId()).isEqualTo(3L);

        repository.deleteById(2L);

        Cliente cuarto = repository.save(new Cliente(null, "44444444-4", "Cuatro", "cuatro@mail.com", "+5694"));
        assertThat(cuarto.getId()).isEqualTo(4L);

        primero.setNombre("Uno Actualizado");
        repository.save(primero);

        assertThat(repository.findAll()).hasSize(3);
        assertThat(repository.findById(1L)).get()
                .extracting(Cliente::getNombre)
                .isEqualTo("Uno Actualizado");
    }

    @Test
    void saveYDeleteById_persistenInmediatamenteEnDisco() {
        Cliente guardado = repository.save(
                new Cliente(null, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222")
        );

        JsonDataStore storeLectura = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeLectura.inicializar();
        ClienteRepository repoLectura = new ClienteRepository(storeLectura);

        assertThat(repoLectura.findAll()).hasSize(1);
        assertThat(repoLectura.findById(guardado.getId())).isPresent();

        boolean eliminado = repository.deleteById(guardado.getId());
        assertThat(eliminado).isTrue();

        JsonDataStore storeTrasEliminar = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeTrasEliminar.inicializar();
        ClienteRepository repoTrasEliminar = new ClienteRepository(storeTrasEliminar);

        assertThat(repoTrasEliminar.findAll()).isEmpty();
    }

    @Test
    void unicidad_verificaRutYEmailExcluyendoPropioId() {
        Cliente c1 = repository.save(new Cliente(null, "12345678-k", "Ana", "ana@mail.com", "+5691"));
        Cliente c2 = repository.save(new Cliente(null, "98765432-1", "Carlos", "carlos@mail.com", "+5692"));

        assertThat(repository.existsByRut("12345678-K")).isTrue();
        assertThat(repository.existsByRut("00000000-0")).isFalse();
        assertThat(repository.existsByEmail("ANA@MAIL.COM")).isTrue();
        assertThat(repository.existsByEmail("otro@mail.com")).isFalse();

        assertThat(repository.existsByRutAndIdNot("12345678-K", c1.getId())).isFalse();
        assertThat(repository.existsByRutAndIdNot("12345678-K", c2.getId())).isTrue();

        assertThat(repository.existsByEmailAndIdNot("ANA@MAIL.COM", c1.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("ANA@MAIL.COM", c2.getId())).isTrue();
    }
}
