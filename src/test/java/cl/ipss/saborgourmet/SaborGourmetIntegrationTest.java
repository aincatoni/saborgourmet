package cl.ipss.saborgourmet;

import cl.ipss.saborgourmet.controller.ClienteController;
import cl.ipss.saborgourmet.controller.MesaController;
import cl.ipss.saborgourmet.controller.ReservaController;
import cl.ipss.saborgourmet.repository.JsonDataStore;
import cl.ipss.saborgourmet.service.ClienteService;
import cl.ipss.saborgourmet.service.MesaService;
import cl.ipss.saborgourmet.service.ReservaService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SaborGourmetIntegrationTest {

    @TempDir
    static Path tempDir;

    private static int primerContextoHash;
    private static LocalDate fechaReserva;

    @DynamicPropertySource
    static void configurarRutaDatos(DynamicPropertyRegistry registry) {
        registry.add("app.datos.ruta", () -> tempDir.resolve("datos.json").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @Order(1)
    @DirtiesContext
    void cicloCompleto_creaReservaCancelaYLiberaDisponibilidad() throws Exception {
        primerContextoHash = System.identityHashCode(applicationContext);
        fechaReserva = LocalDate.now().plusDays(5);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "12345678-9",
                                  "nombre": "Ana Pérez",
                                  "email": "ana@mail.com",
                                  "telefono": "+56911112222"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(post("/api/mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numero": 10,
                                  "capacidad": 4,
                                  "ubicacion": "Terraza",
                                  "activa": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        String reservaJson = """
                {
                  "clienteId": 1,
                  "mesaId": 1,
                  "fecha": "%s",
                  "turno": "CENA_1",
                  "cantidadPersonas": 4,
                  "observaciones": "Aniversario"
                }
                """.formatted(fechaReserva);

        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", fechaReserva.toString())
                        .param("turno", "CENA_1")
                        .param("personas", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(patch("/api/reservas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", fechaReserva.toString())
                        .param("turno", "CENA_1")
                        .param("personas", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numero").value(10));
    }

    @Test
    @Order(2)
    void trasReinicioDeContexto_conservaClientesMesasYReservaCancelada() throws Exception {
        assertThat(System.identityHashCode(applicationContext)).isNotEqualTo(primerContextoHash);
        assertThat(Files.exists(tempDir.resolve("datos.json"))).isTrue();

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rut").value("12345678-9"))
                .andExpect(jsonPath("$[0].nombre").value("Ana Pérez"));

        mockMvc.perform(get("/api/mesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numero").value(10))
                .andExpect(jsonPath("$[0].capacidad").value(4))
                .andExpect(jsonPath("$[0].activa").value(true));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].mesaId").value(1))
                .andExpect(jsonPath("$[0].fecha").value(fechaReserva.toString()))
                .andExpect(jsonPath("$[0].estado").value("CANCELADA"));
    }

    @Test
    @Order(3)
    void arquitecturaCsr_controladoresNoUsanRepositoriosNiJsonDataStoreYAccesoAArchivoAislado() throws IOException {
        List<Class<?>> controllers = List.of(
                ClienteController.class,
                MesaController.class,
                ReservaController.class
        );
        for (Class<?> controller : controllers) {
            for (Field field : controller.getDeclaredFields()) {
                assertThat(esRepositorioODataStore(field.getType()))
                        .as("El campo %s.%s no debe depender de Repository ni JsonDataStore",
                                controller.getSimpleName(), field.getName())
                        .isFalse();
            }
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                for (Class<?> paramType : constructor.getParameterTypes()) {
                    assertThat(esRepositorioODataStore(paramType))
                            .as("El constructor de %s no debe recibir %s",
                                    controller.getSimpleName(), paramType.getSimpleName())
                            .isFalse();
                }
            }
        }

        List<Class<?>> services = List.of(
                ClienteService.class,
                MesaService.class,
                ReservaService.class
        );
        for (Class<?> service : services) {
            for (Field field : service.getDeclaredFields()) {
                assertThat(field.getType())
                        .as("El servicio %s no debe inyectar JsonDataStore", service.getSimpleName())
                        .isNotEqualTo(JsonDataStore.class);
            }
            for (Constructor<?> constructor : service.getDeclaredConstructors()) {
                for (Class<?> paramType : constructor.getParameterTypes()) {
                    assertThat(paramType)
                            .as("El constructor de %s no debe recibir JsonDataStore", service.getSimpleName())
                            .isNotEqualTo(JsonDataStore.class);
                }
            }
        }

        Path srcMainJava = Path.of("src/main/java");
        assertThat(Files.isDirectory(srcMainJava)).isTrue();

        List<Path> archivosJava;
        try (Stream<Path> stream = Files.walk(srcMainJava)) {
            archivosJava = stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .toList();
        }
        assertThat(archivosJava).isNotEmpty();

        for (Path archivo : archivosJava) {
            if ("JsonDataStore.java".equals(archivo.getFileName().toString())) {
                continue;
            }
            String contenido = Files.readString(archivo);
            assertThat(contenido)
                    .as("%s no debe acceder directamente a Files ni ObjectMapper", archivo)
                    .doesNotContain("java.nio.file.Files")
                    .doesNotContain("Files.")
                    .doesNotContain("ObjectMapper");
        }
    }

    private static boolean esRepositorioODataStore(Class<?> tipo) {
        return tipo.equals(JsonDataStore.class)
                || tipo.getSimpleName().endsWith("Repository")
                || "cl.ipss.saborgourmet.repository".equals(tipo.getPackageName());
    }
}
