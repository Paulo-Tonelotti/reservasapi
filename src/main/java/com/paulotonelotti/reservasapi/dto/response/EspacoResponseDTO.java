package com.paulotonelotti.reservasapi.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.paulotonelotti.reservasapi.entity.Espaco;

public record EspacoResponseDTO(
    UUID id,
    String nome,
    Integer capacidade,
    BigDecimal valorPorHora
) {
    public EspacoResponseDTO(Espaco espaco) {
        this(
            espaco.getId(),
            espaco.getNome(),
            espaco.getCapacidade(),
            espaco.getValorPorHora());
    }
}
