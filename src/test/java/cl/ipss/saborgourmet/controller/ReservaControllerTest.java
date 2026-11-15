package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.ReservaResponse;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.service.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @Test
    void crear_conJsonMalformadoOFechaImposible_retorna400ProblemDetailSinStacktrace() throws Exception {
        MvcResult fechaInvalida = mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fecha": "no-es-fecha" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud malformada"))
                .andExpect(jsonPath("$.detail").value("Formato de solicitud o parámetro inválido"))
                .andReturn();

        assertThat(fechaInvalida.getResponse().getContentAsString())
                .doesNotContain("tools.jackson", "DateTimeParseException", "Exception", "at ");

        MvcResult jsonCorrupto = mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{corrupto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud malformada"))
                .andReturn();

        assertThat(jsonCorrupto.getResponse().getContentAsString())
                .doesNotContain("tools.jackson", "JsonParseException", "Exception", "at ");
    }

    @Test
    void listar_conYSinFiltros_delegaAlServicioYRetorna200() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 11, 20);
        ReservaResponse activa = new ReservaResponse(
                1L, 1L, 2L, fecha, Turno.CENA_1, 4, EstadoReserva.ACTIVA, "Ventana"
        );
        when(reservaService.listar(fecha, EstadoReserva.ACTIVA)).thenReturn(List.of(activa));
        when(reservaService.listar(null, null)).thenReturn(List.of(activa));

        mockMvc.perform(get("/api/reservas")
                        .param("fecha", "2026-11-20")
                        .param("estado", "ACTIVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));

        verify(reservaService).listar(fecha, EstadoReserva.ACTIVA);

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(reservaService).listar(null, null);
    }

    @Test
    void obtener_reservaInexistente_retorna404ProblemDetail() throws Exception {
        when(reservaService.obtenerPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("Reserva no encontrada con id: 999"));

        mockMvc.perform(get("/api/reservas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("Reserva no encontrada con id: 999"));
    }
}
