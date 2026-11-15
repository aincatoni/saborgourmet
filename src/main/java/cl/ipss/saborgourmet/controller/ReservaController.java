package cl.ipss.saborgourmet.controller;

import cl.ipss.saborgourmet.dto.ReservaRequest;
import cl.ipss.saborgourmet.dto.ReservaResponse;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
 * Controlador REST para gestionar reservas en {@code /api/reservas}.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Construye el controlador con el servicio de reservas inyectado.
     *
     * @param reservaService servicio de reservas
     */
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /**
     * Lista reservas aplicando filtros opcionales de fecha y estado.
     *
     * @param fecha  fecha exacta (opcional)
     * @param estado estado de la reserva (opcional)
     * @return 200 OK con la lista de reservas
     */
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoReserva estado) {
        return ResponseEntity.ok(reservaService.listar(fecha, estado));
    }

    /**
     * Obtiene una reserva por su identificador.
     *
     * @param id identificador de la reserva
     * @return 200 OK con la reserva
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    /**
     * Crea una nueva reserva.
     *
     * @param request datos de la reserva
     * @return 201 Created con cabecera {@code Location} y la reserva creada
     */
    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse creada = reservaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    /**
     * Modifica una reserva activa existente.
     *
     * @param id      identificador de la reserva
     * @param request nuevos datos de la reserva
     * @return 200 OK con la reserva actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ReservaRequest request) {
        return ResponseEntity.ok(reservaService.actualizar(id, request));
    }

    /**
     * Cancela una reserva activa cambiando su estado a {@code CANCELADA}.
     *
     * @param id identificador de la reserva
     * @return 200 OK con la reserva cancelada
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelar(id));
    }
}
