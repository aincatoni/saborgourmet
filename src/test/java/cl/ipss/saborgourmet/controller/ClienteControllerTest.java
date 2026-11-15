package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.ClienteRequest;
import cl.ipss.saborgourmet.dto.ClienteResponse;
import cl.ipss.saborgourmet.exception.PersistenciaException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void crear_conJsonValido_retorna201LocationYCuerpo() throws Exception {
        ClienteResponse creado = new ClienteResponse(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");
        when(clienteService.crear(any(ClienteRequest.class))).thenReturn(creado);

        String json = """
                {
                  "rut": "12345678-9",
                  "nombre": "Ana Pérez",
                  "email": "ana@mail.com",
                  "telefono": "+56911112222"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/clientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("12345678-9"))
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"))
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.telefono").value("+56911112222"));
    }

    @Test
    void crear_conDatosInvalidos_retorna400ProblemDetailConMapaErrores() throws Exception {
        String jsonInvalido = """
                {
                  "rut": "",
                  "nombre": "Ana Pérez",
                  "email": "correo-invalido",
                  "telefono": "+56911112222"
                }
                """;

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.errores.rut").exists())
                .andExpect(jsonPath("$.errores.email").exists());
    }

    @Test
    void obtener_clienteInexistente_retorna404ProblemDetail() throws Exception {
        when(clienteService.obtenerPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("Cliente no encontrado con id: 999"));

        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("Cliente no encontrado con id: 999"));
    }

    @Test
    void eliminar_clienteValido_retorna204SinCuerpo() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(clienteService).eliminar(1L);
    }

    @Test
    void listar_cuandoOcurrePersistenciaExceptionOErrorInesperado_retorna500GenericoSinTrazas() throws Exception {
        when(clienteService.listar())
                .thenThrow(new PersistenciaException("Error interno al leer /ruta/secreta/datos.json",
                        new RuntimeException("Detalle técnico oculto")));

        MvcResult result = mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error interno en el servidor"))
                .andReturn();

        String cuerpo = result.getResponse().getContentAsString();
        assertThat(cuerpo).doesNotContain("PersistenciaException", "/ruta/secreta", "Detalle técnico oculto", "at cl.ipss");

        doThrow(new IllegalStateException("Falla inesperada")).when(clienteService).listar();
        MvcResult resultInesperado = mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Error interno"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error interno en el servidor"))
                .andReturn();

        assertThat(resultInesperado.getResponse().getContentAsString())
                .doesNotContain("IllegalStateException", "Falla inesperada", "at cl.ipss");
    }
}
