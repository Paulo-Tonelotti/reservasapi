package com.paulotonelotti.reservasapi.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato padrão das respostas de erro")
public record ErroResponse(
    @Schema(description = "Momento do erro (UTC)", example = "2026-10-17T17:12:05.385Z")
    Instant timestamp,

    @Schema(description = "Status HTTP", example = "409")
    int status,

    @Schema(description = "Categoria do erro", example = "Operação não permitida")
    String erro,

    @Schema(description = "Descrição do erro", example = "O horário informado já está ocupado")
    String mensagem,

    @Schema(description = "Caminho da requisição", example = "/reservas")
    String caminho
) {
}
