package com.paulotonelotti.reservasapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.paulotonelotti.reservasapi.dto.request.ReservaRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ReservaResponseDTO;
import com.paulotonelotti.reservasapi.entity.Cliente;
import com.paulotonelotti.reservasapi.entity.Espaco;
import com.paulotonelotti.reservasapi.entity.Reserva;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.exceptions.RegraNegocioException;
import com.paulotonelotti.reservasapi.repositories.ReservaRepository;
import com.paulotonelotti.reservasapi.services.ClienteService;
import com.paulotonelotti.reservasapi.services.EspacoService;
import com.paulotonelotti.reservasapi.services.ReservaService;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private EspacoService espacoService;

    @InjectMocks
    private ReservaService reservaService;

    private UUID clienteId;
    private UUID espacoId;
    private Cliente cliente;
    private Espaco espaco;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private ReservaRequestDTO request;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        espacoId = UUID.randomUUID();

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Ana Souza");
        cliente.setEmail("ana.souza@example.com");

        espaco = new Espaco();
        espaco.setId(espacoId);
        espaco.setNome("Auditório");

        inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        fim = inicio.plusHours(2);

        request = new ReservaRequestDTO(clienteId, espacoId, inicio, fim);
    }

    @Test
    void criarReserva_semConflito_salvaReservaPendente() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fim)).thenReturn(false);

        ReservaResponseDTO response = reservaService.criarReserva(request);

        // Captura a reserva que o service mandou salvar, para conferir como ela foi montada
        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(captor.capture());
        Reserva reservaSalva = captor.getValue();

        assertSame(cliente, reservaSalva.getCliente());
        assertSame(espaco, reservaSalva.getEspaco());
        assertEquals(inicio, reservaSalva.getInicio());
        assertEquals(fim, reservaSalva.getFim());
        assertEquals(StatusReserva.PENDENTE, reservaSalva.getStatus());

        assertEquals(clienteId, response.clienteId());
        assertEquals(espacoId, response.espacoId());
        assertEquals(StatusReserva.PENDENTE, response.status());
    }

    @Test
    void criarReserva_comConflito_lancaExcecaoENaoSalva() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fim)).thenReturn(true);

        assertThrows(RegraNegocioException.class,
                () -> reservaService.criarReserva(request));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criarReserva_clienteNaoEncontrado_lancaExcecaoSemConsultarConflito() {
        when(clienteService.buscarEntidadePorId(clienteId))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado: " + clienteId));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.criarReserva(request));

        verifyNoInteractions(espacoService);
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void criarReserva_espacoNaoEncontrado_lancaExcecaoSemConsultarConflito() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId))
                .thenThrow(new RecursoNaoEncontradoException("Espaço não encontrado: " + espacoId));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.criarReserva(request));

        verifyNoInteractions(reservaRepository);
    }
}
