package cl.ipss.saborgourmet.repository;

import cl.ipss.saborgourmet.exception.PersistenciaException;
import cl.ipss.saborgourmet.model.BaseDatos;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.model.Turno;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonDataStoreTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void inicializar_cuandoArchivoNoExiste_creaDirectorioYJsonVacio() throws IOException {
        Path ruta = tempDir.resolve("sub/datos.json");
        JsonDataStore store = new JsonDataStore(ruta.toString(), objectMapper);

        store.inicializar();

        assertThat(Files.exists(ruta)).isTrue();
        assertThat(store.getDatos().getClientes()).isEmpty();
        assertThat(store.getDatos().getMesas()).isEmpty();
        assertThat(store.getDatos().getReservas()).isEmpty();

        String contenido = Files.readString(ruta);
        assertThat(contenido).contains("\"clientes\"", "\"mesas\"", "\"reservas\"");

        BaseDatos leida = objectMapper.readValue(ruta.toFile(), BaseDatos.class);
        assertThat(leida.getClientes()).isEmpty();
        assertThat(leida.getMesas()).isEmpty();
        assertThat(leida.getReservas()).isEmpty();
    }

    @Test
    void inicializar_cuandoArchivoExiste_cargaRegistrosEnMemoria() {
        Path ruta = tempDir.resolve("datos.json");
        BaseDatos inicial = new BaseDatos();
        inicial.setClientes(List.of(new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222")));
        inicial.setMesas(List.of(new Mesa(1L, 5, 4, "Salón", true)));
        inicial.setReservas(List.of(new Reserva(
                1L, 1L, 1L, LocalDate.of(2026, 11, 20), Turno.CENA_1, 3, EstadoReserva.ACTIVA, "Ventana"
        )));
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(ruta.toFile(), inicial);

        JsonDataStore store = new JsonDataStore(ruta.toString(), objectMapper);
        store.inicializar();

        assertThat(store.getDatos().getClientes()).hasSize(1);
        Cliente cliente = store.getDatos().getClientes().get(0);
        assertThat(cliente.getId()).isEqualTo(1L);
        assertThat(cliente.getRut()).isEqualTo("12345678-9");
        assertThat(cliente.getNombre()).isEqualTo("Ana Pérez");
        assertThat(cliente.getEmail()).isEqualTo("ana@mail.com");
        assertThat(cliente.getTelefono()).isEqualTo("+56911112222");

        assertThat(store.getDatos().getMesas()).hasSize(1);
        Mesa mesa = store.getDatos().getMesas().get(0);
        assertThat(mesa.getId()).isEqualTo(1L);
        assertThat(mesa.getNumero()).isEqualTo(5);
        assertThat(mesa.getCapacidad()).isEqualTo(4);
        assertThat(mesa.getUbicacion()).isEqualTo("Salón");
        assertThat(mesa.isActiva()).isTrue();

        assertThat(store.getDatos().getReservas()).hasSize(1);
        Reserva reserva = store.getDatos().getReservas().get(0);
        assertThat(reserva.getId()).isEqualTo(1L);
        assertThat(reserva.getClienteId()).isEqualTo(1L);
        assertThat(reserva.getMesaId()).isEqualTo(1L);
        assertThat(reserva.getFecha()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(reserva.getTurno()).isEqualTo(Turno.CENA_1);
        assertThat(reserva.getCantidadPersonas()).isEqualTo(3);
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.ACTIVA);
        assertThat(reserva.getObservaciones()).isEqualTo("Ventana");
    }

    @Test
    void inicializar_cuandoJsonCorrupto_lanzaPersistenciaExceptionYSinSobrescribir() throws IOException {
        Path ruta = tempDir.resolve("datos.json");
        String corrupto = "{corrupto";
        Files.writeString(ruta, corrupto);

        JsonDataStore store = new JsonDataStore(ruta.toString(), objectMapper);

        assertThatThrownBy(store::inicializar)
                .isInstanceOf(PersistenciaException.class);
        assertThat(Files.readString(ruta)).isEqualTo(corrupto);
    }

    @Test
    void guardar_escribeDeFormaAtomicaSinDejarArchivoTmp() throws IOException {
        Path ruta = tempDir.resolve("datos.json");
        JsonDataStore store = new JsonDataStore(ruta.toString(), objectMapper);
        store.inicializar();

        store.getDatos().getClientes().add(
                new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222")
        );
        store.guardar();

        assertThat(Files.exists(ruta)).isTrue();
        assertThat(Files.exists(ruta.resolveSibling("datos.json.tmp"))).isFalse();
        assertThat(Files.readString(ruta)).contains("Ana Pérez", "12345678-9");

        BaseDatos leida = objectMapper.readValue(ruta.toFile(), BaseDatos.class);
        assertThat(leida.getClientes()).hasSize(1);
    }
}
