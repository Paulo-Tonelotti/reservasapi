package com.paulotonelotti.reservasapi.dto.response;

import java.time.Instant;

public record ErroResponse(
    Instant timestamp,
    int status,
    String erro,
    String mensagem,
    String caminho
) {
}
