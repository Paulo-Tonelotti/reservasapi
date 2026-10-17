package com.paulotonelotti.reservasapi.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ReservaRequestDTO(

    @NotNull(message = "O id do cliente é obrigatório")
    UUID clienteId,

    @NotNull(message = "O id do espaço é obrigatório")
    UUID espacoId,

    @NotNull(message = "A data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    LocalDateTime inicio,

    @NotNull(message = "A data de fim é obrigatória")
    LocalDateTime fim
) {
    @AssertTrue(message = "A data de fim deve ser posterior à data de início")
    public boolean isDataFimPosteriorDataInicio() {
        if (inicio == null || fim == null) {
            return true;
        }

        return fim.isAfter(inicio);
    }
}
