package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.MesaRequest;
import cl.ipss.saborgourmet.dto.MesaResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Mesa;
import cl.ipss.saborgourmet.model.Turno;
import cl.ipss.saborgourmet.repository.MesaRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private MesaService mesaService;

    @Test
    void crearYActualizar_conNumeroDuplicado_lanzaConflictoException() {
        MesaRequest request = new MesaRequest(5, 4, "Salón", true);

        // Crear con número duplicado
        when(mesaRepository.existsByNumero(5)).thenReturn(true);
        assertThatThrownBy(() -> mesaService.crear(request))
                .isInstanceOf(ConflictoException.class);

        // Actualizar con número de otra mesa
        Mesa existente = new Mesa(1L, 1, 2, "Salón", true);
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(mesaRepository.existsByNumeroAndIdNot(5, 1L)).thenReturn(true);
        assertThatThrownBy(() -> mesaService.actualizar(1L, request))
                .isInstanceOf(ConflictoException.class);

        verify(mesaRepository, never()).save(any());

        // Actualizar conservando su propio número
        MesaRequest mismoNumero = new MesaRequest(1, 6, "Terraza", true);
        when(mesaRepository.existsByNumeroAndIdNot(1, 1L)).thenReturn(false);
        when(mesaRepository.save(any(Mesa.class))).thenAnswer(inv -> inv.getArgument(0));

        MesaResponse actualizada = mesaService.actualizar(1L, mismoNumero);
        assertThat(actualizada.numero()).isEqualTo(1);
        assertThat(actualizada.capacidad()).isEqualTo(6);
        assertThat(actualizada.ubicacion()).isEqualTo("Terraza");
    }

    @Test
    void eliminar_mesaConReservaActiva_lanzaConflictoExceptionYSinBorrar() {
        Mesa existente = new Mesa(1L, 1, 4, "Salón", true);
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.existsActivaByMesaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> mesaService.eliminar(1L))
                .isInstanceOf(ConflictoException.class);

        verify(mesaRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_mesaSinReservaActiva_eliminaCorrectamente() {
        Mesa existente = new Mesa(1L, 1, 4, "Salón", true);
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.existsActivaByMesaId(1L)).thenReturn(false);

        mesaService.eliminar(1L);

        verify(mesaRepository).deleteById(1L);

        when(mesaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mesaService.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void disponibles_filtraInactivasCapacidadYOcupadasYOrdenaPorCapacidadAscendente() {
        LocalDate fecha = LocalDate.now().plusDays(5);
        Turno turno = Turno.CENA_1;

        Mesa inactiva = new Mesa(1L, 1, 4, "Barra", false);
        Mesa capacidadInsuficiente = new Mesa(2L, 2, 2, "Salón", true);
        Mesa ocupada = new Mesa(3L, 3, 6, "Terraza", true);
        Mesa libreGrande = new Mesa(4L, 4, 8, "Privado", true);
        Mesa conReservaCancelada = new Mesa(5L, 5, 4, "Salón", true);

        when(mesaRepository.findAll()).thenReturn(
                List.of(inactiva, capacidadInsuficiente, ocupada, libreGrande, conReservaCancelada)
        );
        when(reservaRepository.existsActivaByMesaFechaTurno(3L, fecha, turno, null)).thenReturn(true);
        when(reservaRepository.existsActivaByMesaFechaTurno(4L, fecha, turno, null)).thenReturn(false);
        when(reservaRepository.existsActivaByMesaFechaTurno(5L, fecha, turno, null)).thenReturn(false);

        List<MesaResponse> resultado = mesaService.disponibles(fecha, turno, 4);

        assertThat(resultado)
                .extracting(MesaResponse::id)
                .containsExactly(5L, 4L);
        assertThat(resultado)
                .extracting(MesaResponse::capacidad)
                .containsExactly(4, 8);
    }
}
