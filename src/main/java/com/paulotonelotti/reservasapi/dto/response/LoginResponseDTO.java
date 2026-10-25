package com.paulotonelotti.reservasapi.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para o usuário que logou no sistema")
public record LoginResponseDTO(
        @Schema(description = "Token JWT para enviar no cabeçalho Authorization",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIuLi4ifQ.assinatura")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo,

        @Schema(description = "Momento em que o token deixa de valer (UTC)", example = "2026-10-25T18:30:00Z")
        Instant expiraEm
) {
}
