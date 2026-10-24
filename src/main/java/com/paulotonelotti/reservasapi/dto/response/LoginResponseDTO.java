package com.paulotonelotti.reservasapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Dados para o usuario que logou ao sistema")
public record LoginResponseDTO(
        String token,
        String tipo,
        Instant expiraEm
) {
}
