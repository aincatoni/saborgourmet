package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.MesaRequest;
import cl.ipss.saborgourmet.dto.MesaResponse;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.service.MesaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar mesas y disponibilidad en {@code /api/mesas}.
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    /**
     * Construye el controlador con el servicio de mesas inyectado.
     *
     * @param mesaService servicio de mesas
     */
    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /**
     * Lista todas las mesas registradas.
     *
     * @return 200 OK con la lista de mesas
     */
    @GetMapping
    public ResponseEntity<List<MesaResponse>> listar() {
        return ResponseEntity.ok(mesaService.listar());
    }

    /**
     * Obtiene una mesa por su identificador.
     *
     * @param id identificador de la mesa
     * @return 200 OK con la mesa
     */
    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.obtenerPorId(id));
    }

    /**
     * Consulta las mesas disponibles para una fecha, turno y cantidad de personas.
     *
     * @param fecha    fecha consultada (formato ISO {@code yyyy-MM-dd})
     * @param turno    turno horario
     * @param personas cantidad mínima de comensales (positiva)
     * @return 200 OK con las mesas disponibles ordenadas por capacidad
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<MesaResponse>> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Turno turno,
            @RequestParam @Positive(message = "La cantidad de personas debe ser positiva") int personas) {
        return ResponseEntity.ok(mesaService.disponibles(fecha, turno, personas));
    }

    /**
     * Crea una nueva mesa.
     *
     * @param request datos de la mesa
     * @return 201 Created con cabecera {@code Location} y la mesa creada
     */
    @PostMapping
    public ResponseEntity<MesaResponse> crear(@Valid @RequestBody MesaRequest request) {
        MesaResponse creada = mesaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    /**
     * Actualiza una mesa existente.
     *
     * @param id      identificador de la mesa
     * @param request nuevos datos de la mesa
     * @return 200 OK con la mesa actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<MesaResponse> actualizar(@PathVariable Long id,
                                                   @Valid @RequestBody MesaRequest request) {
        return ResponseEntity.ok(mesaService.actualizar(id, request));
    }

    /**
     * Elimina una mesa si no tiene reservas activas.
     *
     * @param id identificador de la mesa
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
