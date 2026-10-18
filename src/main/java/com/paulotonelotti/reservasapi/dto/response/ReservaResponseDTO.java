package com.paulotonelotti.reservasapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Reserva;
import com.paulotonelotti.reservasapi.entity.enums.StatusReserva;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reserva de um espaço por um cliente")
public record ReservaResponseDTO(
    @Schema(description = "Id da reserva", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    UUID id,

    @Schema(description = "Id do cliente", example = "b0f103f2-1528-4d41-8c0d-189fd5d84982")
    UUID clienteId,

    @Schema(description = "Nome do cliente", example = "Joao Silva")
    String nomeCliente,

    @Schema(description = "Id do espaço", example = "210161ef-ace5-44c0-866e-4364831c72be")
    UUID espacoId,

    @Schema(description = "Nome do espaço", example = "Auditório")
    String nomeEspaco,

    @Schema(description = "Início da reserva", example = "2026-12-01T10:00:00")
    LocalDateTime inicio,

    @Schema(description = "Fim da reserva", example = "2026-12-01T12:00:00")
    LocalDateTime fim,

    @Schema(description = "Valor total, calculado e congelado no momento da reserva", example = "1200.00")
    BigDecimal valorTotal,

    @Schema(description = "Status atual da reserva", example = "PENDENTE")
    StatusReserva status
) {
    public ReservaResponseDTO(Reserva reserva) {
        this(
            reserva.getId(),
            reserva.getCliente().getId(),
            reserva.getCliente().getNome(),
            reserva.getEspaco().getId(),
            reserva.getEspaco().getNome(),
            reserva.getInicio(),
            reserva.getFim(),
            reserva.getValorTotal(),
            reserva.getStatus());
    }
}
