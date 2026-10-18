package com.paulotonelotti.reservasapi.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criar uma reserva")
public record ReservaRequestDTO(

    @Schema(description = "Id do cliente que está reservando", example = "b0f103f2-1528-4d41-8c0d-189fd5d84982")
    @NotNull(message = "O id do cliente é obrigatório")
    UUID clienteId,

    @Schema(description = "Id do espaço reservado", example = "210161ef-ace5-44c0-866e-4364831c72be")
    @NotNull(message = "O id do espaço é obrigatório")
    UUID espacoId,

    @Schema(description = "Início da reserva (deve estar no futuro)", example = "2026-12-01T10:00:00")
    @NotNull(message = "A data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    LocalDateTime inicio,

    @Schema(description = "Fim da reserva (deve ser posterior ao início)", example = "2026-12-01T12:00:00")
    @NotNull(message = "A data de fim é obrigatória")
    LocalDateTime fim
) {
    @Schema(hidden = true)
    @AssertTrue(message = "A data de fim deve ser posterior à data de início")
    public boolean isDataFimPosteriorDataInicio() {
        if (inicio == null || fim == null) {
            return true;
        }

        return fim.isAfter(inicio);
    }
}
