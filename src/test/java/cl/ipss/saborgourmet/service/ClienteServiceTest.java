package cl.ipss.saborgourmet.service;

import cl.ipss.saborgourmet.dto.ClienteRequest;
import cl.ipss.saborgourmet.dto.ClienteResponse;
import cl.ipss.saborgourmet.exception.ConflictoException;
import cl.ipss.saborgourmet.exception.RecursoNoEncontradoException;
import cl.ipss.saborgourmet.model.Cliente;
import cl.ipss.saborgourmet.repository.ClienteRepository;
import cl.ipss.saborgourmet.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void crearYActualizar_conRutOEmailDuplicado_lanzaConflictoException() {
        ClienteRequest request = new ClienteRequest("12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");

        // Crear con RUT duplicado
        when(clienteRepository.existsByRut("12345678-9")).thenReturn(true);
        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(ConflictoException.class);

        // Crear con email duplicado
        when(clienteRepository.existsByRut("12345678-9")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@mail.com")).thenReturn(true);
        assertThatThrownBy(() -> clienteService.crear(request))
                .isInstanceOf(ConflictoException.class);

        // Actualizar con RUT de otro cliente
        Cliente existente = new Cliente(1L, "11111111-1", "Original", "orig@mail.com", "+56900000000");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByRutAndIdNot("12345678-9", 1L)).thenReturn(true);
        assertThatThrownBy(() -> clienteService.actualizar(1L, request))
                .isInstanceOf(ConflictoException.class);

        // Actualizar con email de otro cliente
        when(clienteRepository.existsByRutAndIdNot("12345678-9", 1L)).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdNot("ana@mail.com", 1L)).thenReturn(true);
        assertThatThrownBy(() -> clienteService.actualizar(1L, request))
                .isInstanceOf(ConflictoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void actualizar_conservandoPropioRutYEmail_actualizaSinError() {
        Cliente existente = new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");
        ClienteRequest request = new ClienteRequest("12345678-9", "Ana M. Pérez", "ana@mail.com", "+56999998888");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByRutAndIdNot("12345678-9", 1L)).thenReturn(false);
        when(clienteRepository.existsByEmailAndIdNot("ana@mail.com", 1L)).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.actualizar(1L, request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.rut()).isEqualTo("12345678-9");
        assertThat(response.nombre()).isEqualTo("Ana M. Pérez");
        assertThat(response.email()).isEqualTo("ana@mail.com");
        assertThat(response.telefono()).isEqualTo("+56999998888");
        verify(clienteRepository).save(existente);
    }

    @Test
    void eliminar_clienteConReservaActiva_lanzaConflictoExceptionYSinBorrar() {
        Cliente existente = new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.existsActivaByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.eliminar(1L))
                .isInstanceOf(ConflictoException.class);

        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    void eliminar_clienteSoloConReservasCanceladas_eliminaCorrectamente() {
        Cliente existente = new Cliente(1L, "12345678-9", "Ana Pérez", "ana@mail.com", "+56911112222");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.existsActivaByClienteId(1L)).thenReturn(false);

        clienteService.eliminar(1L);

        verify(clienteRepository).deleteById(1L);

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> clienteService.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
