package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.MesaResponse;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.service.MesaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MesaController.class)
class MesaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MesaService mesaService;

    @Test
    void disponibles_conParametrosInvalidosOFaltantes_retorna400() throws Exception {
        // Sin parámetro personas -> MissingServletRequestParameterException
        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", "2026-11-20")
                        .param("turno", "CENA_1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud malformada"));

        // personas=0 -> HandlerMethodValidationException
        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", "2026-11-20")
                        .param("turno", "CENA_1")
                        .param("personas", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"));

        // turno inválido -> MethodArgumentTypeMismatchException
        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", "2026-11-20")
                        .param("turno", "INVALIDO")
                        .param("personas", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud malformada"));

        // Caso válido -> 200 OK
        LocalDate fecha = LocalDate.of(2026, 11, 20);
        when(mesaService.disponibles(fecha, Turno.CENA_1, 4))
                .thenReturn(List.of(new MesaResponse(2L, 2, 4, "Terraza", true)));

        mockMvc.perform(get("/api/mesas/disponibles")
                        .param("fecha", "2026-11-20")
                        .param("turno", "CENA_1")
                        .param("personas", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].capacidad").value(4));
    }

    @Test
    void obtener_mesaInexistente_retorna404ProblemDetail() throws Exception {
        when(mesaService.obtenerPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("Mesa no encontrada con id: 999"));

        mockMvc.perform(get("/api/mesas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("Mesa no encontrada con id: 999"));
    }

    @Test
    void eliminar_mesaValida_retorna204SinCuerpo() throws Exception {
        mockMvc.perform(delete("/api/mesas/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(mesaService).eliminar(1L);
    }
}
