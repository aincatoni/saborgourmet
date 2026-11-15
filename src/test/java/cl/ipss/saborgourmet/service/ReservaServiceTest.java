package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.ReservaRequest;
import cl.ipss.saborgourmet.dto.ReservaResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.exception.ReglaNegocioException;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.model.EstadoReserva;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Reserva;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MesaRepository mesaRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Cliente clienteActivo() {
        return new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");
    }

    private Mesa mesaActiva(int capacidad) {
        return new Mesa(2L, 2, capacidad, "Terraza", true);
    }

    @Test
    void crearYActualizar_conClienteOMesaInexistente_lanzaRecursoNoEncontradoException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(99L, 2L, fecha, Turno.CENA_1, 2, "");

        // Crear con cliente inexistente
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(RecursoNoEncontradoException.class);

        // Crear con mesa inexistente
        ReservaRequest requestMesaInexistente = new ReservaRequest(1L, 99L, fecha, Turno.CENA_1, 2, "");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo()));
        when(mesaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.crear(requestMesaInexistente))
                .isInstanceOf(RecursoNoEncontradoException.class);

        // Actualizar reserva inexistente
        when(reservaRepository.findById(50L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaService.actualizar(50L, request))
                .isInstanceOf(RecursoNoEncontradoException.class);

        // Actualizar con cliente inexistente
        Reserva activa = new Reserva(10L, 1L, 2L, fecha, Turno.CENA_1, 2, EstadoReserva.ACTIVA, "");
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(activa));
        assertThatThrownBy(() -> reservaService.actualizar(10L, request))
                .isInstanceOf(RecursoNoEncontradoException.class);

        // Actualizar con mesa inexistente
        assertThatThrownBy(() -> reservaService.actualizar(10L, requestMesaInexistente))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crear_conFechaAnteriorAHoy_lanzaReglaNegocioException() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        ReservaRequest request = new ReservaRequest(1L, 2L, ayer, Turno.CENA_1, 2, "");

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(ReglaNegocioException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crear_conMesaInactiva_lanzaConflictoException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(1L, 2L, fecha, Turno.CENA_1, 2, "");
        Mesa inactiva = new Mesa(2L, 6, 4, "Barra", false);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo()));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(inactiva));

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(ConflictoException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crear_conCantidadPersonasMayorACapacidad_lanzaReglaNegocioException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(1L, 2L, fecha, Turno.CENA_1, 6, "");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo()));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesaActiva(4)));

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(ReglaNegocioException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void crear_conMesaFechaTurnoOcupado_lanzaConflictoException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        ReservaRequest request = new ReservaRequest(1L, 2L, fecha, Turno.CENA_1, 4, "");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo()));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesaActiva(4)));
        when(reservaRepository.existsActivaByMesaFechaTurno(2L, fecha, Turno.CENA_1, null)).thenReturn(true);

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(ConflictoException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void actualizar_manteniendoMismaMesaFechaTurno_excluyePropioIdYActualiza() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        Reserva existente = new Reserva(10L, 1L, 2L, fecha, Turno.CENA_1, 2, EstadoReserva.ACTIVA, "Inicial");
        ReservaRequest request = new ReservaRequest(1L, 2L, fecha, Turno.CENA_1, 4, "Actualizada");

        when(reservaRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo()));
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesaActiva(4)));
        when(reservaRepository.existsActivaByMesaFechaTurno(2L, fecha, Turno.CENA_1, 10L)).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse response = reservaService.actualizar(10L, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.cantidadPersonas()).isEqualTo(4);
        assertThat(response.observaciones()).isEqualTo("Actualizada");
        verify(reservaRepository).existsActivaByMesaFechaTurno(2L, fecha, Turno.CENA_1, 10L);
        verify(reservaRepository).save(existente);
    }

    @Test
    void actualizar_reservaCancelada_lanzaConflictoException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        Reserva cancelada = new Reserva(10L, 1L, 2L, fecha, Turno.CENA_1, 2, EstadoReserva.CANCELADA, "");
        ReservaRequest request = new ReservaRequest(1L, 2L, fecha, Turno.CENA_2, 2, "");

        when(reservaRepository.findById(10L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> reservaService.actualizar(10L, request))
                .isInstanceOf(ConflictoException.class);

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void cancelar_reservaActivaPasaACanceladaYSegundaVezLanzaConflictoException() {
        LocalDate fecha = LocalDate.now().plusDays(3);
        Reserva reserva = new Reserva(10L, 1L, 2L, fecha, Turno.CENA_1, 3, EstadoReserva.ACTIVA, "");

        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse primera = reservaService.cancelar(10L);
        assertThat(primera.estado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(reservaRepository, times(1)).save(reserva);

        assertThatThrownBy(() -> reservaService.cancelar(10L))
                .isInstanceOf(ConflictoException.class);
        verify(reservaRepository, times(1)).save(any());
    }
}
