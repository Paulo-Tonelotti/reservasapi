package com.paulotonelotti.reservasapi.exceptions.dto;

import java.time.Instant;
import java.util.List;


public record ErroValidacaoResponse(
    Instant timestamp,
    int status,
    String erro,
    String mensagem,
    String caminho,
    List<CampoErro> erros
) {
    public record CampoErro(String campo, String mensagem) {}
}
