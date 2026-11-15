package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.model.Turno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaRepositoryTest {

    @TempDir
    Path tempDir;

    private Path rutaArchivo;
    private JsonDataStore store;
    private ReservaRepository repository;

    @BeforeEach
    void setUp() {
        rutaArchivo = tempDir.resolve("datos.json");
        store = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        store.inicializar();
        repository = new ReservaRepository(store);
    }

    @Test
    void save_generaIdSecuencialMaxMasUno() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        Reserva r1 = repository.save(new Reserva(null, 1L, 1L, fecha, Turno.ALMUERZO_1, 2, EstadoReserva.ACTIVA, ""));
        assertThat(r1.getId()).isEqualTo(1L);

        // Simular lista con IDs [1, 3]
        store.getDatos().getReservas().add(
                new Reserva(3L, 1L, 2L, fecha, Turno.CENA_1, 4, EstadoReserva.ACTIVA, "")
        );

        Reserva r4 = repository.save(new Reserva(null, 2L, 3L, fecha, Turno.CENA_2, 2, EstadoReserva.ACTIVA, ""));
        assertThat(r4.getId()).isEqualTo(4L);
    }

    @Test
    void save_alCrearYCancelar_persisteInmediatamenteEnDisco() {
        LocalDate fecha = LocalDate.now().plusDays(5);
        Reserva creada = repository.save(
                new Reserva(null, 1L, 2L, fecha, Turno.CENA_1, 4, EstadoReserva.ACTIVA, "Cumpleaños")
        );

        JsonDataStore storeTrasCrear = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeTrasCrear.inicializar();
        ReservaRepository repoTrasCrear = new ReservaRepository(storeTrasCrear);
        assertThat(repoTrasCrear.findById(creada.getId())).get()
                .extracting(Reserva::getEstado)
                .isEqualTo(EstadoReserva.ACTIVA);

        creada.setEstado(EstadoReserva.CANCELADA);
        repository.save(creada);

        JsonDataStore storeTrasCancelar = new JsonDataStore(rutaArchivo.toString(), new ObjectMapper());
        storeTrasCancelar.inicializar();
        ReservaRepository repoTrasCancelar = new ReservaRepository(storeTrasCancelar);
        assertThat(repoTrasCancelar.findById(creada.getId())).get()
                .extracting(Reserva::getEstado)
                .isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void findByFiltros_yConsultasDeReservasActivas() {
        LocalDate fecha1 = LocalDate.now().plusDays(2);
        LocalDate fecha2 = LocalDate.now().plusDays(4);

        Reserva activa = repository.save(
                new Reserva(null, 1L, 10L, fecha1, Turno.CENA_1, 2, EstadoReserva.ACTIVA, "")
        );
        Reserva cancelada = repository.save(
                new Reserva(null, 2L, 20L, fecha2, Turno.CENA_1, 4, EstadoReserva.CANCELADA, "")
        );

        assertThat(repository.findByFiltros(null, null)).hasSize(2);
        assertThat(repository.findByFiltros(fecha1, null)).containsExactly(activa);
        assertThat(repository.findByFiltros(null, EstadoReserva.CANCELADA)).containsExactly(cancelada);
        assertThat(repository.findByFiltros(fecha1, EstadoReserva.CANCELADA)).isEmpty();

        assertThat(repository.existsActivaByClienteId(1L)).isTrue();
        assertThat(repository.existsActivaByClienteId(2L)).isFalse();

        assertThat(repository.existsActivaByMesaId(10L)).isTrue();
        assertThat(repository.existsActivaByMesaId(20L)).isFalse();

        assertThat(repository.existsActivaByMesaFechaTurno(10L, fecha1, Turno.CENA_1, null)).isTrue();
        assertThat(repository.existsActivaByMesaFechaTurno(10L, fecha1, Turno.CENA_1, activa.getId())).isFalse();
        assertThat(repository.existsActivaByMesaFechaTurno(10L, fecha1, Turno.CENA_1, 999L)).isTrue();
        assertThat(repository.existsActivaByMesaFechaTurno(20L, fecha2, Turno.CENA_1, null)).isFalse();
    }
}
