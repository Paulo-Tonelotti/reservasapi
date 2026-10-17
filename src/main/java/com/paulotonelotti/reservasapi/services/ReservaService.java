package com.paulotonelotti.reservasapi.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paulotonelotti.reservasapi.dto.request.ReservaRequestDTO;
import com.paulotonelotti.reservasapi.dto.response.ReservaResponseDTO;
import com.paulotonelotti.reservasapi.entity.Cliente;
import com.paulotonelotti.reservasapi.entity.Espaco;
import com.paulotonelotti.reservasapi.entity.Reserva;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;
import com.paulotonelotti.reservasapi.exceptions.RecursoNaoEncontradoException;
import com.paulotonelotti.reservasapi.exceptions.RegraNegocioException;
import com.paulotonelotti.reservasapi.repositories.ReservaRepository;

@Service
@Transactional(readOnly = true)
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ClienteService clienteService;
    private final EspacoService espacoService;

    public ReservaService(
            ReservaRepository reservaRepository, ClienteService clienteService, EspacoService espacoService) {
        this.reservaRepository = reservaRepository;
        this.clienteService = clienteService;
        this.espacoService = espacoService;
    }

    @Transactional
    public ReservaResponseDTO criarReserva(ReservaRequestDTO reservaRequestDTO) {
        Cliente cliente = clienteService.buscarEntidadePorId(reservaRequestDTO.clienteId());
        Espaco espaco = espacoService.buscarEntidadePorIdComLock(reservaRequestDTO.espacoId());

        if (reservaRepository.existeConflito(
                reservaRequestDTO.espacoId(), reservaRequestDTO.inicio(), reservaRequestDTO.fim())) {
            throw new RegraNegocioException("O horário informado já está ocupado");
        }

        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setEspaco(espaco);
        reserva.setInicio(reservaRequestDTO.inicio());
        reserva.setFim(reservaRequestDTO.fim());
        reserva.setStatus(StatusReserva.PENDENTE);

        reservaRepository.save(reserva);

        return new ReservaResponseDTO(reserva);
    }

    public Page<ReservaResponseDTO> listar(UUID espacoId,
                                           UUID clienteId,
                                           StatusReserva status,
                                           Pageable pageable) {
        Page<Reserva> reservas = reservaRepository.buscarComFiltros(espacoId,
                                                    clienteId,
                                                    status,
                                                    pageable);
        return reservas.map(ReservaResponseDTO::new);
    }

    public Reserva buscarEntidadePorId(UUID id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        String.format("Reserva não encontrada: %s", id)));
    }

    public ReservaResponseDTO buscarPorId(UUID id) {
        Reserva reserva = buscarEntidadePorId(id);
        return new ReservaResponseDTO(reserva);
    }

    @Transactional
    public ReservaResponseDTO confirmarReserva(UUID id) {
        Reserva reserva = buscarEntidadePorId(id);

        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new RegraNegocioException("Não é possível confirmar uma reserva que não está pendente");
        }

        reserva.setStatus(StatusReserva.CONFIRMADA);

        return new ReservaResponseDTO(reserva);
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(UUID id) {
        Reserva reserva = buscarEntidadePorId(id);

        if (reserva.getStatus() != StatusReserva.CONFIRMADA
                && reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new RegraNegocioException(
                    "Não é possível cancelar uma reserva com status " + reserva.getStatus());
        }

        reserva.setStatus(StatusReserva.CANCELADA);

        return new ReservaResponseDTO(reserva);
    }
}
