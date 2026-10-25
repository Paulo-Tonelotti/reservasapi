package com.paulotonelotti.reservasapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
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
import com.paulotonelotti.reservasapi.security.UsuarioLogado;
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

    // Quem está fazendo a requisição (o que o controller monta a partir do token)
    private UsuarioLogado dono;
    private UsuarioLogado outroCliente;
    private UsuarioLogado admin;

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
        espaco.setValorPorHora(new BigDecimal("600.00"));

        inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        fim = inicio.plusHours(2);

        request = new ReservaRequestDTO(espacoId, inicio, fim);

        dono = new UsuarioLogado(clienteId, false);
        outroCliente = new UsuarioLogado(UUID.randomUUID(), false);
        admin = new UsuarioLogado(null, true);
    }

    // ---------- criarReserva ----------

    @Test
    void criarReserva_semConflito_salvaReservaPendente() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fim)).thenReturn(false);

        ReservaResponseDTO response = reservaService.criarReserva(clienteId, request);

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
    void criarReserva_duasHoras_calculaValorTotal() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fim)).thenReturn(false);

        ReservaResponseDTO response = reservaService.criarReserva(clienteId, request);

        // compareTo ignora a escala (1200.0 e 1200.00 são iguais); equals não
        assertEquals(0, new BigDecimal("1200.00").compareTo(response.valorTotal()));
    }

    @Test
    void criarReserva_umaHoraETrintaMinutos_cobraProporcional() {
        LocalDateTime fimFracionado = inicio.plusMinutes(90);
        ReservaRequestDTO requestFracionado = new ReservaRequestDTO(espacoId, inicio, fimFracionado);

        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fimFracionado)).thenReturn(false);

        ReservaResponseDTO response = reservaService.criarReserva(clienteId, requestFracionado);

        assertEquals(0, new BigDecimal("900.00").compareTo(response.valorTotal()));
    }

    @Test
    void criarReserva_comConflito_lancaExcecaoENaoSalva() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId)).thenReturn(espaco);
        when(reservaRepository.existeConflito(espacoId, inicio, fim)).thenReturn(true);

        assertThrows(RegraNegocioException.class,
                () -> reservaService.criarReserva(clienteId, request));

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void criarReserva_clienteNaoEncontrado_lancaExcecaoSemConsultarConflito() {
        when(clienteService.buscarEntidadePorId(clienteId))
                .thenThrow(new RecursoNaoEncontradoException("Cliente não encontrado: " + clienteId));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.criarReserva(clienteId, request));

        verifyNoInteractions(espacoService);
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void criarReserva_espacoNaoEncontrado_lancaExcecaoSemConsultarConflito() {
        when(clienteService.buscarEntidadePorId(clienteId)).thenReturn(cliente);
        when(espacoService.buscarEntidadePorIdComLock(espacoId))
                .thenThrow(new RecursoNaoEncontradoException("Espaço não encontrado: " + espacoId));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.criarReserva(clienteId, request));

        verifyNoInteractions(reservaRepository);
    }

    // ---------- confirmarReserva ----------

    @Test
    void confirmarReserva_pendente_ficaConfirmada() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaResponseDTO response = reservaService.confirmarReserva(reserva.getId());

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(StatusReserva.CONFIRMADA, response.status());
    }

    @Test
    void confirmarReserva_jaConfirmada_lancaExcecaoEMantemStatus() {
        Reserva reserva = reservaComStatus(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThrows(RegraNegocioException.class,
                () -> reservaService.confirmarReserva(reserva.getId()));

        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
    }

    @Test
    void confirmarReserva_cancelada_lancaExcecaoEMantemStatus() {
        Reserva reserva = reservaComStatus(StatusReserva.CANCELADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThrows(RegraNegocioException.class,
                () -> reservaService.confirmarReserva(reserva.getId()));

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
    }

    @Test
    void confirmarReserva_inexistente_lancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        when(reservaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.confirmarReserva(idInexistente));
    }

    // ---------- cancelarReserva ----------

    @Test
    void cancelarReserva_pendente_ficaCancelada() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaResponseDTO response = reservaService.cancelarReserva(reserva.getId(), dono);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        assertEquals(StatusReserva.CANCELADA, response.status());
    }

    @Test
    void cancelarReserva_confirmada_ficaCancelada() {
        Reserva reserva = reservaComStatus(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        reservaService.cancelarReserva(reserva.getId(), dono);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
    }

    @Test
    void cancelarReserva_jaCancelada_lancaExcecao() {
        Reserva reserva = reservaComStatus(StatusReserva.CANCELADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThrows(RegraNegocioException.class,
                () -> reservaService.cancelarReserva(reserva.getId(), dono));
    }

    @Test
    void cancelarReserva_inexistente_lancaRecursoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        when(reservaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.cancelarReserva(idInexistente, dono));
    }

    @Test
    void cancelarReserva_deOutroCliente_lancaRecursoNaoEncontradoEMantemStatus() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        RecursoNaoEncontradoException ex = assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.cancelarReserva(reserva.getId(), outroCliente));

        // A mensagem é a mesma de uma reserva inexistente, para não revelar que o id existe
        assertEquals("Reserva não encontrada: " + reserva.getId(), ex.getMessage());
        assertEquals(StatusReserva.PENDENTE, reserva.getStatus());
    }

    @Test
    void cancelarReserva_deOutroClienteJaCancelada_naoRevelaOStatus() {
        Reserva reserva = reservaComStatus(StatusReserva.CANCELADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        // O acesso é verificado antes da regra de status: o 404 vem antes do 409
        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.cancelarReserva(reserva.getId(), outroCliente));
    }

    @Test
    void cancelarReserva_adminCancelaReservaDeQualquerCliente() {
        Reserva reserva = reservaComStatus(StatusReserva.CONFIRMADA);
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        reservaService.cancelarReserva(reserva.getId(), admin);

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
    }

    // ---------- buscarPorId ----------

    @Test
    void buscarPorId_dono_veAReserva() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findComDetalhesById(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaResponseDTO response = reservaService.buscarPorId(reserva.getId(), dono);

        assertEquals(reserva.getId(), response.id());
    }

    @Test
    void buscarPorId_deOutroCliente_lancaRecursoNaoEncontrado() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findComDetalhesById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> reservaService.buscarPorId(reserva.getId(), outroCliente));
    }

    @Test
    void buscarPorId_admin_veReservaDeQualquerCliente() {
        Reserva reserva = reservaComStatus(StatusReserva.PENDENTE);
        when(reservaRepository.findComDetalhesById(reserva.getId())).thenReturn(Optional.of(reserva));

        ReservaResponseDTO response = reservaService.buscarPorId(reserva.getId(), admin);

        assertEquals(reserva.getId(), response.id());
    }

    // Monta uma reserva já existente (como se tivesse vindo do banco) no status informado
    private Reserva reservaComStatus(StatusReserva status) {
        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setCliente(cliente);
        reserva.setEspaco(espaco);
        reserva.setInicio(inicio);
        reserva.setFim(fim);
        reserva.setValorTotal(new BigDecimal("1200.00"));
        reserva.setStatus(status);
        return reserva;
    }
}
