package com.paulotonelotti.reservasapi.exceptions.dto;

import java.time.Instant;


public record ErroResponse(
    Instant timestamp,
    int status,
    String erro,
    String mensagem,
    String caminho
) {
    
}
